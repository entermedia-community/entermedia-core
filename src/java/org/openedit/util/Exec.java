/*
 * Created on May 4, 2006
 */
package org.openedit.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.OpenEditException;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

public class Exec
{
	private static final Log log = LogFactory.getLog(Exec.class);

	protected long fieldTimeLimit = 3600000L; //1h max, make video conversions be longer

	protected String fieldXmlCommandsFilename;
	protected HashMap<String, ExecCommand> fieldCachedCommands;
	protected XmlArchive fieldXmlArchive;
	protected File fieldRoot;
	protected OutputFiller fieldFiller;
	protected Boolean fieldOnWindows;
	protected ExecutorManager fieldExecutorManager;
	protected Map fieldLongRunningProcesses;
	
	public RunningProcess getProcess(String inName)
	{
		RunningProcess process = (RunningProcess)getLongRunningProcesses().get(inName);
		if( process  == null)
		{
			process = new RunningProcess();
			process.setExecutorManager(getExecutorManager());
			process.start(inName);
			getLongRunningProcesses().put(inName,process);
		}
		return process;
	}
	
	public Map getLongRunningProcesses()
	{
		if (fieldLongRunningProcesses == null)
		{
			fieldLongRunningProcesses = new HashMap();
		}

		return fieldLongRunningProcesses;
	}
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

	public long getTimeLimit()
	{
		return fieldTimeLimit;
	}

	public void setTimeLimit(long inTimelimit)
	{
		fieldTimeLimit = inTimelimit;
	}
	/**
	 * @deprecated Use a command key
	 * @param inArgs
	 * @return
	 */
	public ExecResult runExec(List<String> inArgs)
	{
		return runExec(inArgs, null, false);
	}

	/**
	 * @deprecated Use a command key
	 */
	public ExecResult runExec(List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inArgs, null, inSaveOutput);
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs)
	{
		return runExec(inCommandKey, inArgs, getTimeLimit());
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs, long inTimeout)
	{
		return runExec(inCommandKey, inArgs, false, null, inTimeout);
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs, File inRootFolder)
	{
		return runExec(inCommandKey, inArgs, false, inRootFolder, getTimeLimit());
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput)
	{
		return runExec(inCommandKey, inArgs, inSaveOutput, getTimeLimit());
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput, long inTimeout)
	{
		return runExec(inCommandKey, inArgs, inSaveOutput, null, inTimeout);
	}

	public ExecResult runExec(String inCommandKey, List<String> inArgs, boolean inSaveOutput, File inRootFolder, long inTimeout)
	{
		ArrayList<String> command = new ArrayList<String>();
		//check for cached version
		ExecCommand cachedCommand = findCommand(inCommandKey);
		command.add(cachedCommand.inCommand);
		if (inArgs != null && inArgs.size() > 0)
		{
			command.addAll(command.size(), inArgs);
		}
		if (inRootFolder == null)
		{
			return runExec(command, cachedCommand.inStartDir, inSaveOutput, inTimeout);
		}
		else
		{
			return runExec(command, inRootFolder, inSaveOutput, inTimeout);
		}
	}

	protected ExecCommand findCommand(String inCommandKey)
	{
		ExecCommand cachedCommand = (ExecCommand) fieldCachedCommands.get(inCommandKey);
		if (cachedCommand == null)
		{
			cachedCommand = lookUpCommand(inCommandKey);
		}
		return cachedCommand;
	}
	public ExecResult runExec(List<String> com, File inRunFrom, boolean inSaveOutput) throws OpenEditException
	{
		return runExec(com, inRunFrom, inSaveOutput, getTimeLimit());
	}

	public ExecResult runExec(List<String> com, File inRunFrom, boolean inSaveOutput, long inTimeout) throws OpenEditException
	{
		if( inTimeout == -1)
		{
			inTimeout = getTimeLimit();
		}
		log.info("Running: " + com); 

		FinalizedProcessBuilder pb = new FinalizedProcessBuilder(com).keepProcess(false).logInputtStream(inSaveOutput);
		if(isOnWindows()) 
		{ 
			pb.environment().put("HOME", inRunFrom.getAbsolutePath()); 
		} 
		ExecResult result = new ExecResult();
		try
		{
			FinalizedProcess process = pb.start(getExecutorManager());
			try
			{
				int returnVal = process.waitFor(inTimeout);
				
				if (inSaveOutput)
				{
					result.setStandardOut(process.getStandardOutputs());
				}
				if (returnVal == 0) 
				{
					result.setRunOk(true); 
				} 
				result.setReturnValue(returnVal);
				
			}
			finally
			{
				//Stream should be read in fully then it returns the code
				process.close();
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
			result.setRunOk(false);
			result.setReturnValue(1); //0 is success 1 is error
			String error = result.getStandardError(); 
			if(error == null)
			{
				error = "";
			}
			error = error + ex.toString();
			result.setStandardError(error);
		}
		return result;
	}

	public ExecResult runExecStream(String inCommandKey, List<String> args, OutputStream inOutput, long inTimeout) throws OpenEditException
	{
		ExecCommand cachedCommand = findCommand(inCommandKey);
		List com = new ArrayList(args.size() + 1);
		com.add(cachedCommand.inCommand);
		com.addAll(args);
		log.info("Running: " + com); 

		FinalizedProcessBuilder pb = new FinalizedProcessBuilder(com);

		ExecResult result = new ExecResult();
		try
		{
			FinalizedProcess process = pb.startPipe(getExecutorManager());
			try
			{
				InputStream resultingdata = process.getInputStream();
				int returnVal = process.waitFor(inTimeout);
				getFiller().fill(resultingdata, inOutput);
				if (returnVal == 0) 
				{
					result.setRunOk(true); 
				} 
				result.setReturnValue(returnVal);
			}
			finally
			{
				//Stream should be read in fully then it returns the code
				process.close();
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
			result.setRunOk(false);
			result.setReturnValue(1); //0 is success 1 is error
			String error = result.getStandardError(); 
			if(error == null)
			{
				error = "";
			}
			error = error + ex.toString();
			result.setStandardError(error);
		}
		return result;
	}


	public String getXmlCommandsFilename()
	{
		return fieldXmlCommandsFilename;
	}

	public void setXmlCommandsFilename(String xmlCommands)
	{
		fieldXmlCommandsFilename = xmlCommands;
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive xmlArchive)
	{
		fieldXmlArchive = xmlArchive;
	}

	public File getRoot()
	{
		return fieldRoot;
	}

	public void setRoot(File root)
	{
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

	protected ExecCommand lookUpCommand(String inCommandKey)
	{
		ExecCommand cachedCommand = null;
		//we need to search the xml file
		XmlFile file = fieldXmlArchive.getXml(fieldXmlCommandsFilename, "commandmaps");
		if (file != null)
		{
			String os = System.getProperty("os.name").toUpperCase();
			Iterator<Element> iter = (Iterator) file.getElements("commandmap");
			while (iter.hasNext())
			{
				// check for correct os
				Element map = (Element) iter.next();
				String mapOs = map.attributeValue("os");
				if (mapOs != null && os.contains(mapOs))
				{
					cachedCommand = new ExecCommand();
					String commandBase = map.elementText("commandbase");
					if (commandBase != null)
					{
						commandBase = commandBase.replace('\\', '/'); //Make sure all commands are in Linux notation for now
						if (commandBase.startsWith("./") || commandBase.startsWith("../"))
						{
							String root = getRoot().getAbsolutePath();
							root = root.replace('\\', '/');
							if (root.endsWith("/"))
							{
								root = root.substring(0, root.length() - 1);
							}
							commandBase = PathUtilities.buildRelative(commandBase, root);
						}
						//commandBase = commandBase.replace('\\', '/'); //Make sure all commands are in Linux notation for now
					}
					else
					{
						commandBase = getRoot().getAbsolutePath();
					}
					String commandText = map.elementText(inCommandKey);
					if (commandText == null) //Did not exists
					{
						cachedCommand.inCommand = inCommandKey;
						cachedCommand.inStartDir = new File(commandBase);
					}
					else
					{
						if (commandText.startsWith("./") || commandText.startsWith(".\\") || commandText.startsWith("../") || commandText.startsWith("..\\"))
						{
							commandText = commandText.replace('\\', '/'); //Make sure all commands are in Linux notation for now
							String commandline = PathUtilities.buildRelative(commandText, commandBase);
							File commandfile = new File(commandline);
							cachedCommand.inStartDir = commandfile.getParentFile();
							cachedCommand.inCommand = commandfile.getAbsolutePath();
						}
						else
						{
							cachedCommand.inStartDir = new File(commandBase); //TODO: Use the command for the parent dir?
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
		if (cachedCommand == null)
		{
			cachedCommand = new ExecCommand();
			cachedCommand.inCommand = inCommandKey;
			fieldCachedCommands.put(inCommandKey, cachedCommand);
		}
		return cachedCommand;
	}

	
	
	
	class ExecCommand
	{
		protected String inCommand;
		protected File inStartDir;
	}

}
