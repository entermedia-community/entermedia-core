/*
 * Created on Nov 20, 2006
 */
package org.openedit.page;

import org.openedit.BaseTestCase;
import org.openedit.page.Page;

public class PageSettingsTest extends BaseTestCase
{
	
	public void testSubStitute() throws Exception
	{
		Page page = getFixture().getPageManager().getPage("/replace.html");
		assertEquals("New + default",page.getProperty("title"));
	}
}
