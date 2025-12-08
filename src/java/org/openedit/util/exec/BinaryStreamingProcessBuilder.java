package org.openedit.util.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A ProcessBuilder wrapper that handles binary streaming using ExecutorService.
 * This ensures that input and error streams are consumed asynchronously before
 * waiting for the process to complete, preventing deadlocks.
 */
public class BinaryStreamingProcessBuilder {
    
    private ProcessBuilder processBuilder;
    private long timeout = 0;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private byte[] inputData;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStream errorStream;
    
    public BinaryStreamingProcessBuilder(String... command) {
        this.processBuilder = new ProcessBuilder(command);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    public BinaryStreamingProcessBuilder(List<String> command) {
        this.processBuilder = new ProcessBuilder(command);
        this.executorService = Executors.newFixedThreadPool(2);
    }
  
    protected ExecutorService executorService;
    
    public ExecutorService getExecutorService()
	{
		return executorService;
	}

	public void setExecutorService(ExecutorService inExecutorService)
	{
		executorService = inExecutorService;
	}

	/**
     * Set input data to send to the process's stdin
     */
    public BinaryStreamingProcessBuilder withInput(byte[] inputData) {
        this.inputData = inputData;
        this.inputStream = null;
        return this;
    }
    
    /**
     * Set input stream to send to the process's stdin
     */
    public BinaryStreamingProcessBuilder withInput(InputStream inputStream) {
        this.inputStream = inputStream;
        this.inputData = null;
        return this;
    }
    
    /**
     * Set input string to send to the process's stdin
     */
    public BinaryStreamingProcessBuilder withInput(String input) {
        this.inputData = input.getBytes();
        this.inputStream = null;
        return this;
    }
    
    /**
     * Set output stream to receive the process's stdout
     */
    public BinaryStreamingProcessBuilder withOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }
    
    /**
     * Set error stream to receive the process's stderr
     */
    public BinaryStreamingProcessBuilder withErrorStream(OutputStream errorStream) {
        this.errorStream = errorStream;
        return this;
    }
    
    /**
     * Set timeout for process execution
     */
    public BinaryStreamingProcessBuilder withTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.timeoutUnit = unit;
        return this;
    }
    
    /**
     * Get the underlying ProcessBuilder for additional configuration
     */
    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }
    
    /**
     * Execute the process and return the result containing exit code and streams
     */
    public ProcessResult execute() throws IOException, InterruptedException {
        Process process = processBuilder.start();
        
        // Submit stream reading tasks to executor
        Future<byte[]> outputFuture = executorService.submit(() -> 
            outputStream != null ? streamToOutputStream(process.getInputStream(), outputStream) : readStream(process.getInputStream())
        );
        Future<byte[]> errorFuture = executorService.submit(() -> 
            errorStream != null ? streamToOutputStream(process.getErrorStream(), errorStream) : readStream(process.getErrorStream())
        );
        
        // Submit input writing task if input is provided
        Future<Void> inputFuture = null;
        if (inputData != null || inputStream != null) {
            inputFuture = executorService.submit(() -> {
                writeToStream(process.getOutputStream());
                return null;
            });
        }
        
        int exitCode;
        try {
            // Wait for input to be written first if provided
            if (inputFuture != null) {
                inputFuture.get();
            }
            
            // Wait for streams to be consumed first
            byte[] outputData = outputFuture.get();
            byte[] errorData = errorFuture.get();
            
            // Now wait for process to complete
            if (timeout > 0) {
                boolean completed = process.waitFor(timeout, timeoutUnit);
                if (!completed) {
                    process.destroyForcibly();
                    throw new InterruptedException("Process timed out after " + timeout + " " + timeoutUnit);
                }
            } else {
                process.waitFor();
            }
            
            exitCode = process.exitValue();
            
            return new ProcessResult(exitCode, outputData, errorData);
            
        } catch (Exception e) {
            process.destroyForcibly();
            throw new IOException("Error executing process: " + e.getMessage(), e);
//        } finally {
//            if (shutdownExecutorOnComplete) {
//                executorService.shutdown();
//                executorService.awaitTermination(5, TimeUnit.SECONDS);
//            }
        }
    }
    
    /**
     * Read all bytes from an InputStream
     */
    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        buffer.flush();
        return buffer.toByteArray();
    }
    
    /**
     * Stream data from input to output stream without buffering in memory
     */
    private byte[] streamToOutputStream(InputStream inputStream, OutputStream targetStream) throws IOException {
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            targetStream.write(data, 0, bytesRead);
        }
        
        targetStream.flush();
        return new byte[0]; // Return empty array when streaming to avoid memory overhead
    }
    
    /**
     * Write input data to the process's stdin
     */
    private void writeToStream(java.io.OutputStream outputStream) throws IOException {
        try {
            if (inputData != null) {
                outputStream.write(inputData);
            } else if (inputStream != null) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }
    
    /**
     * Result object containing exit code and stream data
     */
    public static class ProcessResult {
        private final int exitCode;
        private final byte[] outputData;
        private final byte[] errorData;
        
        public ProcessResult(int exitCode, byte[] outputData, byte[] errorData) {
            this.exitCode = exitCode;
            this.outputData = outputData;
            this.errorData = errorData;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        /**
         * Get output data. Returns empty array if output was streamed to an OutputStream.
         */
        public byte[] getOutputData() {
            return outputData;
        }
        
        /**
         * Get error data. Returns empty array if error was streamed to an OutputStream.
         */
        public byte[] getErrorData() {
            return errorData;
        }
        
        /**
         * Get output as string. Returns empty string if output was streamed to an OutputStream.
         */
        public String getOutputAsString() {
            return new String(outputData);
        }
        
        /**
         * Get error as string. Returns empty string if error was streamed to an OutputStream.
         */
        public String getErrorAsString() {
            return new String(errorData);
        }
        
        public boolean isSuccess() {
            return exitCode == 0;
        }
        
        /**
         * Check if output data was buffered in memory
         */
        public boolean hasOutputData() {
            return outputData != null && outputData.length > 0;
        }
        
        /**
         * Check if error data was buffered in memory
         */
        public boolean hasErrorData() {
            return errorData != null && errorData.length > 0;
        }
    }
    
    /**
     * Builder-style method to add command arguments
     */
    public BinaryStreamingProcessBuilder command(String... command) {
        processBuilder.command(command);
        return this;
    }
    
    /**
     * Builder-style method to add command arguments
     */
    public BinaryStreamingProcessBuilder command(List<String> command) {
        processBuilder.command(command);
        return this;
    }
    
    /**
     * Add a single argument to the command
     */
    public BinaryStreamingProcessBuilder addArgument(String arg) {
        List<String> command = new ArrayList<>(processBuilder.command());
        command.add(arg);
        processBuilder.command(command);
        return this;
    }
}
