/*
 * Created on May 4, 2006
 */
package com.openedit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.OpenEditException;

public class Exec
{
	private static final Log log = LogFactory.getLog(Exec.class);
	
	protected int fieldTimeLimit; // an optional timelimit on the exececution time
	protected boolean fieldTimeLimited = false;// set a time limit for this process to complete
	
	protected String fieldXmlCommandsFilename;
	protected HashMap<String, ExecCommand> fieldCachedCommands;
	protected XmlArchive fieldXmlArchive;
	protected File fieldRoot;
	protected OutputFiller fieldFiller;
	protected Boolean fieldOnWindows;
	protected ExecutorManager fieldExecutorManager;
	
	
	public ExecutorManager getExecutorManager()
	{
		if (fieldExecutorManager == null)
		{
			fieldExecutorManager = new ExecutorManager();
		}
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

	public Exec()
	{
		fieldCachedCommands = new HashMap<String, ExecCommand>();
	}
	
	public int getTimeLimit()
	{
		return fieldTimeLimit;
	}

	public void setTimeLimit(int inTimelimit)
	{
		fieldTimeLimit = inTimelimit;
	}

	public boolean isTimeLimited()
	{
		return fieldTimeLimited;
	}

	public void setTimeLimited(boolean inTimeLimited)
	{
		fieldTimeLimited = inTimeLimited;
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, InputStream inPut)
	{
		return runExec(inCommandKey,  inArgs, false, inPut, null, null);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs)
	{
		return runExec(inCommandKey, inArgs,-1);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, long inTimeout)
	{
		return runExec(inCommandKey,  inArgs, false, null, null, null, inTimeout);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, File inRootFolder)
	{
		return runExec(inCommandKey,  inArgs, false, null, null, inRootFolder);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inCommandKey,  inArgs, inSaveOutput, -1);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, boolean inSaveOutput, long inTimeout)
	{
		return runExec(inCommandKey,  inArgs, inSaveOutput, null, null, null, inTimeout);
	}
	
	/**
	 * @deprecated Use a command key
	 * @param inArgs
	 * @return
	 */
	public ExecResult runExec(List<String> inArgs)
	{
		return runExec(inArgs, null, false, null);
	}
	/**
	 * @deprecated Use a command key
	 */
	public ExecResult runExec(List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inArgs, null,inSaveOutput, null);
	}
	
	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput, InputStream inPut, OutputFiller inOutputFiller, File inRootFolder)
	{
		return runExec(inCommandKey,inArgs,inSaveOutput,inPut, inOutputFiller, inRootFolder, -1);
	}
	
	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput, InputStream inPut, OutputFiller inOutputFiller, File inRootFolder, long inTimeout)
	{
		
		ArrayList<String> command = new ArrayList<String>();
		//check for cached version
		ExecCommand cachedCommand = (ExecCommand)fieldCachedCommands.get(inCommandKey);
		if(cachedCommand == null)
		{
			//we need to search the xml file
			XmlFile file = fieldXmlArchive.getXml(fieldXmlCommandsFilename,"commandmaps");
			if (file != null) 
			{
				String os = System.getProperty("os.name").toUpperCase();
				Iterator<Element> iter = (Iterator) file.getElements("commandmap");
				while (iter.hasNext()) {
					// check for correct os
					Element map = (Element) iter.next();
					String mapOs = map.attributeValue("os");
					if (mapOs != null && os.contains(mapOs)) 
					{
						cachedCommand = new ExecCommand();
						String commandBase = map.elementText("commandbase");
						if( commandBase != null)
						{
							commandBase = commandBase.replace('\\', '/'); //Make sure all commands are in Linux notation for now
							if( commandBase.startsWith("./") || commandBase.startsWith("../"))
							{
								String root = getRoot().getAbsolutePath();
								root = root.replace('\\', '/');
								if( root.endsWith("/"))
								{
									root = root.substring(0,root.length() -1);
								}
								commandBase = PathUtilities.buildRelative(commandBase,root);
							}
							//commandBase = commandBase.replace('\\', '/'); //Make sure all commands are in Linux notation for now
						}
						else
						{
							commandBase = getRoot().getAbsolutePath();
						}
						String commandText = map.elementText(inCommandKey);
						if( commandText == null) //Did not exists
						{
							cachedCommand.inCommand = inCommandKey;
							cachedCommand.inStartDir = new File( commandBase);
						}
						else
						{
							if( commandText.startsWith("./") || commandText.startsWith(".\\") || commandText.startsWith("../") || commandText.startsWith("..\\")) 
							{
								commandText = commandText.replace('\\', '/'); //Make sure all commands are in Linux notation for now
								String commandline = PathUtilities.buildRelative(commandText, commandBase);
								File commandfile = new File( commandline );
								cachedCommand.inStartDir = commandfile.getParentFile();
								cachedCommand.inCommand = commandfile.getAbsolutePath();
							}
							else
							{
								cachedCommand.inStartDir = new File( commandBase); //TODO: Use the command for the parent dir?
								cachedCommand.inCommand = commandText;
							}
						}
						fieldCachedCommands.put(inCommandKey, cachedCommand);
						break;
					}
				}
			}
			//there was no trace of the command in the xml file so we will just execute
			//from the system path
			if(cachedCommand == null)
			{
				cachedCommand = new ExecCommand();
				cachedCommand.inCommand = inCommandKey;
				fieldCachedCommands.put(inCommandKey, cachedCommand);
			}
		}
		command.add(cachedCommand.inCommand);
		if(inArgs != null && inArgs.size() > 0)
		{
			command.addAll(command.size(), inArgs);
		}
		if( inRootFolder == null )
		{
			return runExec(command,cachedCommand.inStartDir,inSaveOutput, inPut, inTimeout);
		}
		else
		{
			return runExec(command,inRootFolder,inSaveOutput, inPut, inTimeout);
		}
	}
	class ExecCommand
	{
		protected String inCommand;
		protected File inStartDir;
	}
	
	public ExecResult runExec(List<String> com, File inRunFrom, boolean inSaveOutput, InputStream inputStream) throws OpenEditException
	{
		return runExec(com,inRunFrom,inSaveOutput,inputStream,-1);
	}
	
	public ExecResult runExec(List<String> com, File inRunFrom, boolean inSaveOutput, InputStream inputStream, long inTimeout) throws OpenEditException
	{
		ExecResult result = new ExecResult();
		result.setRunOk(false);
		String[] inCommand = (String[]) com.toArray(new String[com.size()]);
		try
		{
			log.info("Running: " + com + " in " + inRunFrom);
			ProcessBuilder builder = new ProcessBuilder(inCommand);
			if( isOnWindows() )
			{
				//String[] env  = new String[] { "HOME=" + inRunFrom.getAbsolutePath() };
				builder.environment().put("HOME", inRunFrom.getAbsolutePath());
			}
			builder.redirectErrorStream(true);
			if(inRunFrom == null){
				inRunFrom = getRoot();
			}
			builder.directory(inRunFrom);
			
			Process proc = builder.start();//Runtime.getRuntime().exec(inCommand,env,inRunFrom);

			InputStreamHandler reader1 = new InputStreamHandler(inSaveOutput);
			reader1.setStream("stdout",proc.getInputStream());
			getExecutorManager().execute(reader1);

			if(inputStream != null)
			{
				OutputStream out = proc.getOutputStream();
				try
				{
					getFiller().fill(inputStream,out);
					
				}
				finally
				{
					getFiller().close(inputStream);
					getFiller().close(out);
				}
			}
//			InputStreamHandler errreader = new InputStreamHandler(inSaveOutput);
//			errreader.setStream("stderr",proc.getErrorStream());
//			//getExecutorManager().execute(errreader);
//			errreader.run();
			
			int ret = -1;
			long timelimit = inTimeout > 0 ? inTimeout : (long) getTimeLimit();
			if ( timelimit > 0){
				log.info("executing processing with a timeout of "+timelimit+" ms (process hashcode="+proc.hashCode()+")");
				try
			    {
			      synchronized(proc) {
			    	  proc.wait(timelimit);
			      }
			    } catch (InterruptedException e) {}
				try{
					ret = proc.exitValue();
				}catch (IllegalThreadStateException e){
					log.info("unable to retrieve exit value on process (hashcode="+proc.hashCode()+"), process did not complete within allotted time interval ("+timelimit+" ms), setting a return value of -1");
					ret = -1;
					ProcessDestroyer wrap = new ProcessDestroyer();
					wrap.setProcess(proc);
					new Thread(wrap).start();
				}
			} else {
				ret = proc.waitFor();
			}
			
			int tries = 10;
			if( !reader1.isCompleted() )
			{
				synchronized (reader1)
				{
					while( !reader1.isCompleted() )
					{
						reader1.wait(60000);
						tries--;
						if( tries == 0)
						{
							log.error("Waiting over 10 minutes to just read the output of a command " + com);
							reader1.wait();
						}
					}			
				}
			}
			String stdo = reader1.getText();
			if (stdo != null && stdo.length() > 0)
			{
				result.setStandardOut(stdo);
			}
//			String stder = errreader.getText();
//			if (stder != null && stder.length() > 0)
//			{
//				result.setStandardError(stder);
//			}
			if( ret != 0 )
			{
				log.error("Error: " + ret + " stdo:" + stdo + " when running " + com);
			}
			if( ret == 0 )
			{
				result.setRunOk(true);
			}
			result.setReturnValue(ret);
			return result;

		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}
	
	class ProcessDestroyer implements Runnable{
		
		Process process;
		
		public void setProcess(Process inProcess){
			process = inProcess;
		}
		
		public boolean isAlive(){
			try{
				process.exitValue();
				return false;
			} catch (Exception e){};
			return true;
		}

		@Override
		public void run() {
			long ms = System.currentTimeMillis();
			log.info("attempting to kill process with hashcode="+process.hashCode());
			//try to kill process for 5 minutes (max)
			for (int i=0; i<1200 && isAlive(); i++){
				try{
					Thread.sleep(250);
				}catch (Exception e){}
				try{
		    		process.destroy();
		    	}catch(Exception e){}	
			}
			ms = System.currentTimeMillis() - ms;
			if (isAlive()){
				log.error("unable to kill process with hashcode="+process.hashCode());
			} else {
				log.info("successfully killed process with hashcode="+process.hashCode()+", took "+ms+" ms");
			}
		}
	}

	class InputStreamHandler implements Runnable 
	{
		protected String fieldType;
    	protected InputStream fieldStream;
    	protected String fieldText;
    	protected boolean fieldSaveText;
    	protected boolean fieldCompleted;
    	
    	public boolean isCompleted()
		{
			return fieldCompleted;
		}

		public void setCompleted(boolean inCompleted)
		{
			fieldCompleted = inCompleted;
		}

		InputStreamHandler(boolean inSave)
    	{
    		fieldSaveText = inSave;
    	}
    	
    	public String getText()
    	{
    		return fieldText;
    	}
    	public InputStream getStream() 
    	{
    		return fieldStream;
    	}
    	public void setStream(String inType, InputStream inStream) 
    	{
    		fieldType = inType;
    		fieldStream = inStream;
    	}
    	public void run()
    	{
    		try
    		{
    			BufferedReader reader = new BufferedReader(new InputStreamReader(getStream()));
    			if( fieldSaveText )
    			{
	    			StringBuffer writer = new StringBuffer();
	    			String line = null;

			        while ((line = reader.readLine()) != null) 
			        {
			        	if( log.isDebugEnabled() )
			        	{
			        		log.debug(fieldType + " " + line);
			        	}
			        	writer.append(line);
			        	writer.append('\n');
			    		if( writer.length() > 100000 ) //Dont let this buffer get more than 100k of memory
			    		{
			    			String cut = writer.substring(writer.length() - 70000, writer.length());
			    			writer = new StringBuffer(cut);
			    		}
			        }
	    			fieldText = writer.toString();
    			}
    			else
    			{
	    			String line = null;
			        while ((line = reader.readLine()) != null) 
			        {    					
    					if( log.isDebugEnabled() )
			        	{
			        		log.debug(fieldType + " " + line);
			        	}
    				}
    			}
    			
    			if( !isCompleted() )
	        	{
	    			synchronized (this)
					{
	        			setCompleted(true);
	        			this.notifyAll();					
					}
	        	}
    			
    		}
    		catch ( IOException ex)
    		{
    			//ignore?
    			log.error(ex);
    		}
    	}
    }

	public String getXmlCommandsFilename() {
		return fieldXmlCommandsFilename;
	}

	public void setXmlCommandsFilename(String xmlCommands) {
		fieldXmlCommandsFilename = xmlCommands;
	}

	public XmlArchive getXmlArchive() {
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive xmlArchive) {
		fieldXmlArchive = xmlArchive;
	}

	public File getRoot() {
		return fieldRoot;
	}

	public void setRoot(File root) {
		fieldRoot = root;
	}
	
	public String makeAbsolute(String inCommandBase)
	{
		if (inCommandBase.startsWith("./"))
		{
			inCommandBase = new File(getRoot(), inCommandBase.substring(2)).getAbsolutePath();
		}
		if (!inCommandBase.endsWith("/"))
		{
			inCommandBase += "/";
		}
		return inCommandBase;
	}

	public Boolean isOnWindows()
	{
		if (fieldOnWindows == null)
		{
			if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
			{
				fieldOnWindows = Boolean.TRUE;
			}
			else
			{
				fieldOnWindows = Boolean.FALSE;
			}
			
		}
		return fieldOnWindows;
	}
	public void setIsOnWindows(boolean inBoolean)
	{
		fieldOnWindows = inBoolean;
	}

}
