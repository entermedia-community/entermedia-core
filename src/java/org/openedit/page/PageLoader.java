package org.openedit.page;

import org.openedit.servlet.RightPage;
import org.openedit.servlet.Site;
import org.openedit.util.URLUtilities;

public interface PageLoader
{

	public RightPage getRightPage( URLUtilities url, Site site,  Page inPage);
//	{
//		//loads the right page using Java code
//		//Loads all the PageLoader beans in the xconfs
//		PinPage.getPageLoaders()
//		
//		return null;
//	}
	
}
