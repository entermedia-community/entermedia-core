/*
 * Created on Nov 20, 2006
 */
package com.openedit.page;

import com.openedit.BaseTestCase;

public class PageSettingsTest extends BaseTestCase
{
	
	public void testSubStitute() throws Exception
	{
		Page page = getFixture().getPageManager().getPage("/replace.html");
		assertEquals("New + default",page.getProperty("title"));
	}
}
