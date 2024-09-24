package org.openedit.repository;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openedit.OpenEditException;
import org.openedit.util.EmStringUtils;
import org.openedit.util.PathUtilities;

public abstract class BaseRepository implements Repository
{
	protected boolean fieldLoadOnStartup;
	protected String fieldPath;			//This is saved
	protected String fieldExternalPath; //this is saved
	protected String fieldDefaultRemoteDir; //this is saved
 	protected String fieldMatchesPostFix;   //this is saved. Used to limit the matches for only *.xconf for example
	protected String fieldRepositoryType;
	protected String fieldFilterOut;
	protected String fieldFilterIn;
	protected String[] fieldFilterInList;
	protected Map fieldProperties;
 	
//	public String getPrefix()
//	{
//		if( fieldPrefix == null)
//		{
//			String external = getExternalPath();
//			if( external == null)
//			{
//				external = getServerRoot() + getPath(); //point to itself? /webapps + /WEB-INF
//			}
//			else
//			{
//				///home/cburkey/data  + WEB-INF/index.xconf
//				new File(external).mkdirs();
//			}
//			//remove trailing slashes
//			if( external.endsWith("/"))
//			{
//				external = external.substring(0, external.length()-1);
//			}
//			fieldPrefix =	external;
//		}
//		return fieldPrefix;
//	}

	public Map getProperties()
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap();
		}
		return fieldProperties;
	}
	
	
	public void setProperty(String inPropName, String inValue)
	{
		if( inValue == null )
		{
			getProperties().remove(inPropName);
		}
		else
		{
			getProperties().put(inPropName, inValue);
		}
	}
	
	public String getProperty(String inPropName)
	{
		return (String)getProperties().get(inPropName);
	}

	public String getMatchesPostFix()
	{
		return fieldMatchesPostFix;
	}

	public void setMatchesPostFix(String inMatchesPostFix)
	{
		fieldMatchesPostFix = inMatchesPostFix;
	}

	public String getAbsolutePath( String inOePath)
	{
		if( getPath() != null && !getPath().equals("/"))
		{
			//strip off the extra 	path stuff
			inOePath = inOePath.substring(getPath().length());
			if(inOePath.length() == 0){
				inOePath = "/";
			}
		}
		
		String toReturn =  getExternalPath() + inOePath;
		toReturn = toReturn.replace('/', File.separatorChar);
		return toReturn;
		
	}

	protected String[] getFilterInList()
	{
		return fieldFilterInList;
	}

	protected void setFilterInList(String[] inFilterInList)
	{
		fieldFilterInList = inFilterInList;
	}

	protected String[] getFilterOutList()
	{
		return fieldFilterOutList;
	}

	protected void setFilterOutList(String[] inFilterOutList)
	{
		fieldFilterOutList = inFilterOutList;
	}
	protected String[] fieldFilterOutList;
	
	public BaseRepository()
	{
	}
	
	public String getFilterIn()
	{
		return fieldFilterIn;
	}

	public void setFilterIn(String inFilterIn)
	{
		fieldFilterIn = inFilterIn;
		if( fieldFilterIn != null)
		{
			//fieldFilterIn = fieldFilterIn.toLowerCase();
			//setFilterInList( EmStringUtils.split(inFilterIn).toArray() );
			Collection supported = EmStringUtils.split(inFilterIn);
			String[] vals = (String[])supported.toArray(new String[supported.size()]);
//			int i = 0;
//			for (Iterator iterator = supported.iterator(); iterator.hasNext();)
//			{
//				String type = (String) iterator.next();
//				if( !type.startsWith("*"))
//				{
//					type = "*." + type;
//				}
//				vals[i] = type;
//				i++;
//			}
			setFilterInList(vals);
		}
	}

	public String getRepositoryType() {
		return fieldRepositoryType;
	}

	public void setRepositoryType(String repositoryType) {
		fieldRepositoryType = repositoryType;
	}

		
	public BaseRepository(String inPath)
	{
		setPath(inPath);
	}

	public boolean isLoadOnStartup()
	{
		return fieldLoadOnStartup;
	}
	public void setLoadOnStartup(boolean inLoadOnStartup)
	{
		fieldLoadOnStartup = inLoadOnStartup;
	}
	/**
	 * Something like /stuff/*
	 * @return
	 */
	public String getFilterOut()
	{
		return fieldFilterOut;
	}
	public void setFilterOut(String inMatchesPath)
	{
		fieldFilterOut = inMatchesPath;
		if( fieldFilterOut != null)
		{
			fieldFilterOut = fieldFilterOut.toLowerCase();
			Collection supported = EmStringUtils.split(fieldFilterOut);
			//String[] supported = fieldFilterOut.split("\\s+"); // \s includes \n among others
			setFilterOutList((String[])supported.toArray(new String[supported.size()]));
		}
	}
	/**
	 * Absolute path on the disk drive
	 * @return
	 */
	public String getExternalPath()
	{
		return fieldExternalPath;
	}

	public void setExternalPath(String inExternalPath)
	{
		if( inExternalPath != null && inExternalPath.endsWith("/"))
		{
			inExternalPath = inExternalPath.substring(0, inExternalPath.length()-1);
		}
		fieldExternalPath = inExternalPath;
	}

	
	/**
	 * this is the point that OpenEdit is mounted. Most of the time this is /
	 * @return
	 */
	public String getPath()
	{
		if( fieldPath == null )
		{
			return "/";
		}
		return fieldPath;
	}

	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}

//	public boolean isUseVersionControl()
//	{
//		return fieldUseVersionControl;
//	}
//
//	public void setUseVersionControl(boolean inUseVersionControl)
//	{
//		fieldUseVersionControl = inUseVersionControl;
//	}
	protected boolean matchExact(String inPath)
	{
		return (getPath().equals(inPath));
	}
	
	protected boolean pathMatches(String inPath)
	{
		String beginning = getPath();
		if (!beginning.endsWith("/"))
		{
			beginning = beginning + "/"; //Add a slash, because partially matching the directory name
									 //is both bad behavior and a security threat
		}
		if( fieldMatchesPostFix != null)
		{
			return PathUtilities.match(inPath, beginning + getMatchesPostFix());
		}
		return inPath.startsWith(beginning);
	}
	protected String substring(String inPrefix, String inPostFix)
	{
		int index = getPath().indexOf(inPrefix);
		if( index == -1)
		{
			return null;
		}
		int postIndex = getPath().indexOf(inPostFix);
		if(postIndex == -1)
		{
			return null;
		}
		String sub = getPath().substring(index  + inPrefix.length(), postIndex  );
		return sub;
	}
	protected String substring(String inPrefix)
	{
		int index = getPath().indexOf(inPrefix);
		if(index == -1)
		{
			return null;
		}
		String sub = getPath().substring(index  + inPrefix.length() );
		return sub;
	}
	/**
	 * We will see if this is the right config for a certain path
	 * Since the list is sorted we should get the most specific first
	 * @param inPath
	 * @return
	 */
	public boolean matches(String inPath)
	{
		if( matchExact(inPath) || pathMatches(inPath))
			//either the paths match exactly, 
			//or the repository path(ending in a slash) is a subfolder of the target path
		{
			return true;
		}
		return false;
	}
	public String getId()
	{
		return getPath() + getFilterOut();
	}

	protected boolean isExcluded(String inPath)
	{
		if( getFilterOut() != null )
		{
			//inPath = inPath.toLowerCase();
			for( String out: getFilterOutList() )
			{
				if(PathUtilities.match(inPath,out))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void restoreVersion(ContentItem inPath, String inVersion) throws RepositoryException {
		throw new OpenEditException("Not implemented");
	}
	@Override
	public ContentItem getVersion(ContentItem inItem, String inVersion) throws RepositoryException {
		throw new OpenEditException("Not implemented");
	}
}
