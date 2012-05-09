package com.openedit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;

import com.openedit.page.manage.PageManager;
import com.openedit.users.User;

public abstract class PathProcessor
{
	protected String fieldRootPath;
	protected PageManager fieldPageManager;
	protected int fieldExecCount;
	protected List fieldIncludeExtensions;
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
		process(getPageManager().getRepository().getStub(inPath), inUser);
	}

	public void process(String inPath)
	{
		process(getPageManager().getRepository().getStub(inPath), null);
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
		if (getIncludeExtensions() != null)
		{
			String ext = PathUtilities.extractPageType(inItem.getPath());
			if( ext != null)
			{
				for (Iterator iterator = getIncludeExtensions().iterator(); iterator.hasNext();)
				{
					String validExt = (String) iterator.next();
					if (validExt.equals(ext.toLowerCase()))
					{
						return true;
					}
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

	public int getExecCount()
	{
		return fieldExecCount;
	}

	public void setExecCount(int inExecCount)
	{
		fieldExecCount = inExecCount;
	}

	public int incrementCount()
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

	public List getIncludeExtensions()
	{
		return fieldIncludeExtensions;
	}

	public void setIncludeExtensions(List inIncludeExtensions)
	{
		fieldIncludeExtensions = inIncludeExtensions;
	}

	public void setIncludeExtensions(String inIncludeFilter)
	{
		if (inIncludeFilter != null && inIncludeFilter.length() > 0)
		{
			fieldIncludeExtensions = EmStringUtils.split(inIncludeFilter);
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

	
}
