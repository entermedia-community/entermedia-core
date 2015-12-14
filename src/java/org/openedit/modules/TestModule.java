/*
 * Created on Oct 18, 2004
 */
package org.openedit.modules;

import java.util.List;

import org.openedit.WebPageRequest;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class TestModule extends BaseModule
{
	int count = 0;
	int restrictedCount = 0;
	
	public void test( WebPageRequest inContext )
	{
		count++;
		System.out.println( "Called TestModule.test " + count + " times.");
	}
	
	public void testRestricted( WebPageRequest inReq)
	{
		restrictedCount++;
		System.out.println( "Called TestModule.testRestricted " + getRestrictedCount() + " times.");
	}
	
	public int getCount()
	{
		return count;
	}
	
	public int getRestrictedCount()
	{
		return restrictedCount;
	}
	
	public void files (WebPageRequest req) throws Exception {
		if (req.getRequestParameter("path") == null)
			return;
		List list = getPageManager().getChildrenPaths(req.getRequestParameter("path"));
		//for (int i=list.size()-1; i>=0; i--) {
			//String path=(String) list.get(i);
			//if (!getPageManager().getPage(path).isImage())
				//list.remove(i);
			/*else if (path.lastIndexOf('.')!=-1) {
				String ext=path.substring(path.lastIndexOf('.')+1);
				if (!ext.equals("xconf"))
					list.remove(i);
			}*/
		//}
		req.putPageValue("childs", list);
		
	}
	
	public boolean canJump(WebPageRequest inReq)
	{
		return false;
	}
}
