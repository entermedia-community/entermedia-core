/*
 * Created on Jun 3, 2006
 */
package org.openedit.page;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.page.manage.PageManager;
import org.openedit.util.PathUtilities;

public class FileFinder
{
	private static final Log log = LogFactory.getLog(FileFinder.class);
	
	protected File fieldRoot;
	protected PageManager fieldPageManager;
	protected boolean fieldMatchPatterns = true;
	protected boolean fieldRecursive = true;
	protected Set fieldSkipFiles;
	
	protected boolean skip(String inFileName)
	{
		return getSkipFiles().contains(inFileName);
	}
	
	protected Set getSkipFiles()
	{
		if (fieldSkipFiles == null)
		{
			fieldSkipFiles = new HashSet();
			if( isMatchPatterns() )
			{
				fieldSkipFiles.add("*.versions*");
				fieldSkipFiles.add("*/CVS/*");		
			}
			else
			{
				fieldSkipFiles.add(".versions");
				fieldSkipFiles.add("CVS");
			}
		}
		return fieldSkipFiles;
	}
	public void addSkipFileName(String inName)
	{
		getSkipFiles().add(inName);
	}
	
	public List findPages(String inContains) throws OpenEditException
	{
		List paths = new ArrayList();
		File dir = new File(getRoot(), PathUtilities.extractDirectoryPath(inContains));
		findMatches(dir, getRoot().getAbsolutePath().length(), inContains, paths);
		
		List pages = new ArrayList(paths.size());
		
		for (Iterator iter = paths.iterator(); iter.hasNext();)
		{
			String path = (String) iter.next();
			try
			{
				Page page = getPageManager().getPage(path);
				pages.add(page);
			}
			catch ( Exception ex )
			{
				log.error( ex );
				log.error( "could not load " + path);
			}
		}
		getPageManager().clearCache();
		return pages;
	}
	protected void findMatches(File inRoot, int from, String contains, List found)
	{
		if( inRoot.isDirectory())
		{

			File[] children = inRoot.listFiles( createSkipFilter() );
			if( children != null)
			{
				for (int i = 0; i < children.length; i++)
				{
					File child =  children[i];
					if ( child.isDirectory())
					{
						if( isMatchPatterns() )
						{
							if( skipMatches( child.getName()) )
							{
								continue;
							}
						}
						else if ( skip(child.getName()))
						{
							continue;
						}
						if( !isRecursive() )
						{
							continue;
						}
					}
					findMatches(children[i], from, contains, found);
				}
			}
		}
		else
		{
			String path = inRoot.getAbsolutePath().substring(from);
			path = path.replace('\\', '/');
			if( isMatchPatterns() )
			{
				if( PathUtilities.match(path, contains))
				{
					if( !skipMatches( path ))
					{
						found.add(path);
					}
				}
			}
			else 
			{
				if( path.indexOf(contains) > -1 )
				{
					if( !skip(path))
					{
						found.add(path);
					}
				}
			}
		}
	}
	
	protected FileFilter createSkipFilter()
	{
		return new FileFilter()
		{
		
			public boolean accept( File file )
			{
				if ( isMatchPatterns() )
				{
					return !skipMatches( file.getName() );
				}
				else
				{
					return !skip( file.getName() );
				}
			}
		
		};
		
	}

	protected boolean skipMatches(String inPath)
	{
		for (Iterator iterator = getSkipFiles().iterator(); iterator.hasNext();)
		{
			String file	 = (String) iterator.next();
			if( PathUtilities.match(inPath, file))
			{
				return true;
			}
		}

		return false;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	public File getRoot()
	{
		return fieldRoot;
	}
	public void setRoot(File inRoot)
	{
		fieldRoot = inRoot;
	}
	public boolean isMatchPatterns()
	{
		return fieldMatchPatterns;
	}
	public void setMatchPatterns(boolean inUseMatches)
	{
		fieldMatchPatterns = inUseMatches;
	}
	public boolean isRecursive()
	{
		return fieldRecursive;
	}
	public void setRecursive(boolean inRecursive)
	{
		fieldRecursive = inRecursive;
	}

	
}
