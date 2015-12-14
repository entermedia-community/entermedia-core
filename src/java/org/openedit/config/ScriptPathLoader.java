package org.openedit.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScriptPathLoader
{
	protected File fieldRoot;
	
	
	public File getRoot()
	{
		return fieldRoot;
	}


	public void setRoot(File inRoot)
	{
		fieldRoot = inRoot;
	}


	public List<String> findPaths()
	{
		List<String> folders = new ArrayList<String>();
		//Add the base folders
		File custom = new File( getRoot(), "/WEB-INF/src/");
		if( custom.exists() )
		{
			folders.add(custom.getAbsolutePath());
		}
				
		//There is no order to these folder names
		File basefolders = new File( getRoot(), "/WEB-INF/base/");
		File[] children = basefolders.listFiles();
		if( children != null )
		{
			for (int i = 0; i < children.length; i++)
			{
				File script = new File( children[i],"/src/");
				if( script.exists() )
				{
					folders.add(script.getAbsolutePath());
				}
			}
		}
		return folders;
	}
}
