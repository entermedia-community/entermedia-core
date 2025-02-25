package org.openedit.page;

import org.openedit.servlet.RightPage;
import org.openedit.servlet.Site;
import org.openedit.util.URLUtilities;

public interface PageLoader
{

	public RightPage getRightPage( URLUtilities util, Site site,  Page inPage,String requestedPath);
//	{
//		//loads the right page using Java code
//		//Loads all the PageLoader beans in the xconfs
//		PinPage.getPageLoaders()
//		
//		return null;
//	}
	
}
