/*
 * Created on May 4, 2006
 */
package com.openedit.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.OpenEditException;

public class Exec
{
	private static final Log log = LogFactory.getLog(Exec.class);
	
	protected int timelimit; // an optional timelimit on the exececution time
	protected boolean fieldTimeLimited = false;// set a time limit for this process to complete
	
	protected String fieldXmlCommandsFilename;
	protected HashMap<String, ExecCommand> fieldCachedCommands;
	protected XmlArchive fieldXmlArchive;
	protected File fieldRoot;
	protected OutputFiller fieldFiller;
	protected Boolean fieldOnWindows;
	protected Executor fieldExecutor;
	
	
	
	public Executor getExecutor() {
		if (fieldExecutor == null) {
			fieldExecutor = Executors.newCachedThreadPool();
			
		}

		return fieldExecutor;
	}

	public void setExecutor(Executor inExecutor) {
		fieldExecutor = inExecutor;
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
	
	public int getTimelimit()
	{
		return timelimit;
	}

	public void setTimelimit(int inTimelimit)
	{
		timelimit = inTimelimit;
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
		return runExec(inCommandKey,  inArgs, false, inPut, null);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs)
	{
		return runExec(inCommandKey,  inArgs, false, null, null);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inCommandKey,  inArgs, inSaveOutput, null, null);
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
	
	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput, InputStream inPut, OutputFiller inOutputFiller)
	{
		ArrayList<String> command = new ArrayList<String>();
		
		//check for cached version
		ExecCommand cachedCommand = (ExecCommand)fieldCachedCommands.get(inCommandKey);
		if(cachedCommand == null)
		{
			String os = System.getProperty("os.name").toUpperCase();
			//we need to search the xml file
			XmlFile file = fieldXmlArchive.getXml(fieldXmlCommandsFilename,"commandmaps");
			if (file != null) 
			{
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
							if( commandBase.startsWith("./"))
							{
								String root = getRoot().getAbsolutePath();
								root = root.replace('\\', '/');
								if( root.endsWith("/"))
								{
									root = root.substring(0,root.length() -1);
								}
								commandBase  =  root + commandBase.substring(1);
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
							if( commandText.startsWith("./") || commandText.startsWith(".\\"))
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
		
		return runExec(command,cachedCommand.inStartDir,inSaveOutput, inPut);
	}
	class ExecCommand
	{
		protected String inCommand;
		protected File inStartDir;
	}
	
	
	public ExecResult runExec(List<String> com, File inRunFrom, boolean inSaveOutput, InputStream inputStream) throws OpenEditException
	{
		ExecResult result = new ExecResult();
		result.setRunOk(false);
		String[] inCommand = (String[]) com.toArray(new String[com.size()]);
		try
		{
			log.info("Running: " + com + " in " + inRunFrom);
			String[] env = null;
			if( isOnWindows() )
			{
				env  = new String[] { "HOME=" + inRunFrom.getAbsolutePath() };
			}
			
			Process proc = Runtime.getRuntime().exec(inCommand,env,inRunFrom);
			int ret = 0;
			if (inSaveOutput || isOnWindows() ) //windows locks up sometimes unless this is done. Is this still true?
			{
				InputStreamHandler reader1 = new InputStreamHandler();
				reader1.setStream(proc.getInputStream());
				
				getExecutor().execute(reader1);

				InputStreamHandler errreader = new InputStreamHandler();
				errreader.setStream(proc.getErrorStream());
				getExecutor().execute(errreader);
				
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
				
				ret = proc.waitFor();
				result.setReturnValue(ret);
				
				//This might cause a lock up if thread never started?
				reader1.join();
				errreader.join();
				
				String stdo = reader1.getText();
				if (stdo != null && stdo.length() > 0)
				{
					result.setStandardOut(stdo);
				}
				String stder = errreader.getText();
				if (stder != null && stder.length() > 0)
				{
					result.setStandardError(stder);
				}
				if( ret != 0 )
				{
					log.error("Error: " + ret + " stderr: " + stder + " stdo:" + stdo + " when running " + com);
				}
			}
			else
			{
				InputStreamHandler reader1 = new InputStreamHandler();
				reader1.setStream(proc.getInputStream());
				reader1.start();

				InputStreamHandler errreader = new InputStreamHandler();
				errreader.setStream(proc.getErrorStream());
				errreader.start();
				if(inputStream != null)
				{
					OutputStream out = proc.getOutputStream();
					try
					{
						getFiller().fill(inputStream,out); //If the command line is bad this will also fail
					}
					finally
					{
						getFiller().close(inputStream);
						getFiller().close(out);
					}
				}

				ret = proc.waitFor();
			}
			if( ret == 0 )
			{
				result.setRunOk(true);
			}
			return result;

		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}

	class InputStreamHandler extends Thread {

    	protected InputStream fieldStream;
    	protected String fieldText;
    	
    	public String getText()
    	{
    		return fieldText;
    	}
    	public InputStream getStream() 
    	{
    		return fieldStream;
    	}
    	public void setStream(InputStream inStream) 
    	{
    		fieldStream = inStream;
    	}
    	public void run()
    	{
    		try
    		{
    			StringWriter writer = new StringWriter();
    			new OutputFiller().fill(new InputStreamReader(getStream()), writer);
    			fieldText = writer.toString();
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
