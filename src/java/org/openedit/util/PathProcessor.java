package org.openedit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.page.manage.PageManager;
import org.openedit.repository.ContentItem;
import org.openedit.users.User;

public abstract class PathProcessor
{
	protected String fieldRootPath;
	protected PageManager fieldPageManager;
	protected long fieldExecCount;
	protected List fieldIncludeMatches;
	protected List fieldExcludeMatches;
	protected boolean fieldRecursive = true;
	
	private static final Log log = LogFactory.getLog(PathProcessor.class);
	
	public String getRootPath()
	{
		return fieldRootPath;
	}

	public void setRootPath(String inRootPath)
	{
		fieldRootPath = inRootPath;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	
	public List getExcludeMatches()
	{
		return fieldExcludeMatches;
	}


	/**
	 * List of extensions we will include
	 * @param inFilter
	public void setFilter(String inFilter)
	{
		fieldFilter = inFilter;
		if( inFilter != null && inFilter.equals("*.*"))
		{
			fieldFilter = null;
			return;
		}
		if (inFilter != null && inFilter.length() > 0)
		{
			fieldIncludeExtensions = new ArrayList();
			String[] extns = inFilter.split(",");
			for (int i = 0; i < extns.length; i++)
			{
				fieldIncludeExtensions.add(extns[i].trim());
			}
		}
	}
	*/
	public void process()
	{
		List paths = getPageManager().getChildrenPaths(
				getRootPath());
		for (Iterator iterator = paths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			process(path);	
		}
//		process(getRootPath());
	}
	
	public void process(String inPath, User inUser)
	{
		ContentItem item = getPageManager().getRepository().getStub(inPath);
		if( !item.exists() )
		{		
			log.info(item.getAbsolutePath() + " Did not exist");
			return;
		}
		process(item, inUser);
	}

	public void process(String inPath)
	{
		ContentItem item = getPageManager().getRepository().getStub(inPath);
		if( !item.exists() )
		{
			log.info(inPath + " Did not exist");
			return;
		}

		process(item, null);
	}

	public void process(ContentItem inInput, User inUser)
	{
		if (inInput.isFolder())
		{
			if (acceptDir(inInput))
			{
				processDir(inInput);

				List paths = getPageManager().getChildrenPaths(
						inInput.getPath());
				for (Iterator iterator = paths.iterator(); iterator.hasNext();)
				{
					String path = (String) iterator.next();
					ContentItem item = getPageManager().getRepository()
							.getStub(path);
					if( isRecursive() || !item.isFolder() )
					{
						process(item, inUser);
					}
				}
			}
		}
		else
		{
			if (acceptFile(inInput))
			{
				processFile(inInput, inUser);
			}
		}
	}

	public boolean acceptFile(ContentItem inItem)
	{
		if( inItem.getLength() == 0 )
		{
			return false;
		}
//let the mount deal with this  - NO, this presumes there is a mount! there aren't always.
		if (getIncludeMatches() != null)
		{
			//String ext = PathUtilities.extractPageType(inItem.getPath());
			String path =  inItem.getPath();
			for (Iterator iterator = getIncludeMatches().iterator(); iterator.hasNext();)
			{
				String validExt = (String) iterator.next();
				if (PathUtilities.match(path, validExt))
				{
					return true;
				}
			}
			return false; //Include only specific files
		}
		if (fieldExcludeMatches != null)
		{
			String path =  inItem.getPath();
			for (Iterator iterator = getExcludeMatches().iterator(); iterator.hasNext();)
			{
				String match = (String) iterator.next();
				if (PathUtilities.match(path, match))
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean acceptDir(ContentItem inDir)
	{
		String path = inDir.getPath();
		if (path.endsWith("/CVS") || path.endsWith("/.versions") || path.endsWith("/.svn"))
		{
			return false;
		}
		//this is not neede 
//		if (fieldIncludeMatches != null)
//		{
//			for (Iterator iterator = getIncludeMatches().iterator(); iterator.hasNext();)
//			{
//				String match = (String) iterator.next();
//				if (PathUtilities.match(path, match))
//				{
//					return true;
//				}
//			}
//		}
		if (fieldExcludeMatches != null)
		{
			for (Iterator iterator = getExcludeMatches().iterator(); iterator.hasNext();)
			{
				String match = (String) iterator.next();
				if (PathUtilities.match(path, match))
				{
					return false;
				}
			}
		}
		
		return true;
	}

	public long getExecCount()
	{
		return fieldExecCount;
	}

	public void setExecCount(long inExecCount)
	{
		fieldExecCount = inExecCount;
	}

	public long incrementCount()
	{
		fieldExecCount++;
		return fieldExecCount;
	}

	protected File[] findFiles(File inParent, final String inAccept)
	{
		FileFilter filter = new FileFilter() {
			public boolean accept(File inDir)
			{
				String inName = inDir.getName();
				if (inName.startsWith("."))
				{
					return false;
				}
				if (inDir.isDirectory())
				{
					return true;
				}

				if (inAccept != null)
				{
					if (!PathUtilities.match(inDir.getName().toLowerCase(),
							inAccept.toLowerCase()))
					{
						return false;
					}
				}

				return true;
			}
		};
		return inParent.listFiles(filter);
	}
	/**
	 * @deprecated Is this really needed? We use Page Managers now
	 * @param inSearchDirectory
	 * @param inAll
	 * @param inFilter
	 */
	protected void findFiles(File inSearchDirectory, List inAll,
			FileFilter inFilter)
	{
		File[] toadd = inSearchDirectory.listFiles(inFilter);

		for (int i = 0; i < toadd.length; i++)
		{
			File file = toadd[i];
			if (file.isDirectory())
			{
				findFiles(file, inAll, inFilter);
			}
			else
			{
				inAll.add(file);
			}
		}
	}

	public abstract void processFile(ContentItem inContent, User inUser);

	public void processDir(ContentItem inContent)
	{

	}

	public boolean isRecursive()
	{
		return fieldRecursive;
	}

	public void setRecursive(boolean inRecursive)
	{
		fieldRecursive = inRecursive;
	}

	public List getIncludeMatches()
	{
		return fieldIncludeMatches;
	}

	public void setIncludeMatches(List inIncludeExtensions)
	{
		fieldIncludeMatches = inIncludeExtensions;
	}

	public void setIncludeMatches(String inIncludeFilter)
	{
		if (inIncludeFilter != null && inIncludeFilter.length() > 0)
		{
			fieldIncludeMatches = EmStringUtils.split(inIncludeFilter);
		}
		
	}
	/**
	 * Comma separated values
	 * @param inExcludeFilter
	 */
	public void setExcludeMatches(String inExcludeFilter)
	{
		if (inExcludeFilter != null && inExcludeFilter.length() > 0)
		{
			fieldExcludeMatches = EmStringUtils.split(inExcludeFilter);
		}
	}
	public void setExcludeMatches(List inExcludeFilter)
	{
		fieldExcludeMatches = inExcludeFilter;
	}

	
}
