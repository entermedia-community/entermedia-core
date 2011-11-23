package com.openedit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	protected String fieldFilter; // "png,gif"
	protected List fieldIncludeExtensions;
	protected String fieldExcludeFilter;
	protected List fieldExcludeExtensions;
	
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
	
	public List getExcludeExtensions()
	{
		return fieldExcludeExtensions;
	}

	public String getFilter()
	{
		return fieldFilter;
	}

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
		if (getFilter() != null)
		{
			String ext = PathUtilities.extractPageType(inItem.getPath());
			
			for (Iterator iterator = getIncludeExtensions().iterator(); iterator.hasNext();)
			{
				String validExt = (String) iterator.next();
				if (validExt.equalsIgnoreCase(ext))
				{
					return true;
				}
			}
			return false;
		}
		if (getExcludeFilter() != null)
		{
			String ext = PathUtilities.extractPageType(inItem.getPath());
			if( ext != null)
			{
				for (Iterator iterator = getExcludeExtensions().iterator(); iterator.hasNext();)
				{
					String validExt = (String) iterator.next();
					if (validExt.equalsIgnoreCase(ext))
					{
						return false;
					}
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
		if (getExcludeFilter() != null)
		{
			String name = PathUtilities.extractPageName(path);
			for (Iterator iterator = getExcludeExtensions().iterator(); iterator.hasNext();)
			{
				String validExt = (String) iterator.next();
				if (validExt.equalsIgnoreCase(name))
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

	public String getExcludeFilter()
	{
		return fieldExcludeFilter;
	}

	public void setIncludeFileFilter(String inIncludeFilter)
	{
		if (inIncludeFilter != null && inIncludeFilter.length() > 0)
		{
			fieldIncludeExtensions = new ArrayList();
			String[] extns = inIncludeFilter.split(",");
			for (int i = 0; i < extns.length; i++)
			{
				fieldIncludeExtensions.add(extns[i].trim());
			}
		}
		
	}
	public void setExcludeFilter(String inExcludeFilter)
	{
		fieldExcludeFilter = inExcludeFilter;
		if (inExcludeFilter != null && inExcludeFilter.length() > 0)
		{
			fieldExcludeExtensions = new ArrayList();
			String[] extns = inExcludeFilter.split(",");
			for (int i = 0; i < extns.length; i++)
			{
				fieldExcludeExtensions.add(extns[i].trim());
			}
		}

	}

	
}
