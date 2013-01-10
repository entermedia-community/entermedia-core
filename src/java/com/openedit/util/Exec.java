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
import java.io.StringWriter;
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
	
	protected int timelimit; // an optional timelimit on the exececution time
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
		return runExec(inCommandKey,  inArgs, false, inPut, null, null);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs)
	{
		return runExec(inCommandKey,  inArgs, false, null, null, null);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, File inRootFolder)
	{
		return runExec(inCommandKey,  inArgs, false, null, null, inRootFolder);
	}
	public ExecResult runExec(String inCommandKey,List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inCommandKey,  inArgs, inSaveOutput, null, null, null);
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
		if( inRootFolder == null )
		{
			return runExec(command,cachedCommand.inStartDir,inSaveOutput, inPut);
		}
		else
		{
			return runExec(command,inRootFolder,inSaveOutput, inPut);
		}
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

			InputStreamHandler reader1 = new InputStreamHandler(inSaveOutput);
			reader1.setStream(proc.getInputStream());
			getExecutorManager().execute(reader1);

			InputStreamHandler errreader = new InputStreamHandler(inSaveOutput);
			errreader.setStream(proc.getErrorStream());
			getExecutorManager().execute(errreader);
			
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
			
			int ret = proc.waitFor();
			
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

	class InputStreamHandler implements Runnable 
	{

    	protected InputStream fieldStream;
    	protected String fieldText;
    	protected boolean fieldSaveText;
    	
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
    	public void setStream(InputStream inStream) 
    	{
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
    				while ((reader.readLine()) != null) {}
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
