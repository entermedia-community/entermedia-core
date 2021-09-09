package org.openedit.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;

public class RunningProcess
{
	private static final Log log = LogFactory.getLog(Exec.class);
	protected OutputFiller fieldFiller;

	protected ExecutorManager fieldExecutorManager;
	protected Process process;
	Writer sendtoprocesswriter;
	BufferedReader readfromprocess;
	StreamCopyRunner streamcopy;
	String fieldCommandName;
	public StreamCopyRunner getStreamcopy()
	{
		return streamcopy;
	}

	public ExecutorManager getExecutorManager()
	{
		return fieldExecutorManager;
	}

	public void setExecutorManager(ExecutorManager inExecutorManager)
	{
		fieldExecutorManager = inExecutorManager;
	}

	public OutputFiller getFiller()
	{
		if (fieldFiller == null)
		{
			fieldFiller = new OutputFiller();
		}
		return fieldFiller;
	}


	//Called one time
	public void start(String inCommandKey)
	{
		start(inCommandKey,Collections.EMPTY_LIST);
	}
	public void start(String inCommandKey, List<String> args)
	{
		fieldCommandName = inCommandKey;
		List com = new ArrayList(args.size() + 1);
		com.add(inCommandKey);
		com.addAll(args);
		log.info("Starting: " + com); 

		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.redirectErrorStream(true);
			processBuilder.command(com);
			//processBuilder.
			process = processBuilder.start();
		      
			sendtoprocesswriter = new OutputStreamWriter(  process.getOutputStream(), "UTF-8" ); //Send to process
			//Start param thread
			readfromprocess = new BufferedReader( new InputStreamReader(process.getInputStream(),"UTF-8") );
			//int returnVal = process.waitFor();
			streamcopy = new StreamCopyRunner(readfromprocess, getExecutorManager());
			synchronized (streamcopy)
			{
				streamcopy.startCopy();
				streamcopy.wait(2000);				
			}
		}
		catch (Exception ex)
		{
			throw new OpenEditException("Could not start " + fieldCommandName ,ex);
		}
		
	}
	public String runExecStream(String inToSendToProces) throws OpenEditException
	{
		return runExecStream(inToSendToProces, -1);
	}
	public String runExecStream(String inToSendToProces, long timeout) throws OpenEditException
	{
			try
			{
				log.info("Running " + fieldCommandName + " with:" + inToSendToProces);
				sendtoprocesswriter.write(inToSendToProces + "\n");
				sendtoprocesswriter.flush();

//				return null;
				String returned = getStreamcopy().getNextResult(timeout);
				//log.info("Complete " + fieldCommandName + " with:" + inToSendToProces);
				return returned;
				//get the last result from the other thread?
				
			}
			catch (IOException e)
			{
				log.debug("Failed to gobble stream", e);
			}
			return null;
	}

}
