/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.web;

import junit.framework.TestCase;


/**
 * Test for Browser, parsing query strings for different browsers
 *
 * @author Eric Galluzzo
 */
public class BrowserTest extends TestCase
{
	public BrowserTest(String inName)
	{
		super(inName);
	}


	/**
		 *
		 */
	public void testGaleonLinux()
	{
		Browser browser = new Browser(
				"Mozilla/5.0 Galeon/1.2.0 (X11; Linux i686; U;) Gecko/20020408");
		assertEquals(Browser.GECKO_BROWSER, browser.getBrowserType());
		assertEquals("20020408", browser.getVersion());
	}

	/**
		 *
		 */
	public void testLynxLinux()
	{
		Browser browser = new Browser("Lynx/2.8.5dev.3 libwww-FM/2.14 SSL-MM/1.4.1 OpenSSL/0.9.6c");
		assertEquals(Browser.TEXT_BROWSER, browser.getBrowserType());
		assertEquals("2.8.5.3", browser.getVersion());
		assertEquals("2", browser.getMajorVersion());
		assertEquals("8", browser.getMinorVersion());
	}

	/**
		 *
		 */
	public void testMSIEMacOS9()
	{
		Browser browser = new Browser("Mozilla/4.0 (compatible; MSIE 5.0; Mac_PowerPC)");
		assertEquals(Browser.MSIE_BROWSER, browser.getBrowserType());
		assertEquals("5.0", browser.getVersion());
		assertEquals("5", browser.getMajorVersion());
		assertEquals("0", browser.getMinorVersion());
	}

	/**
		 *
		 */
	public void testMSIEMacOSX()
	{
		Browser browser = new Browser("Mozilla/4.0 (compatible; MSIE 5.14; Mac_PowerPC)");
		assertEquals(Browser.MSIE_BROWSER, browser.getBrowserType());
		assertEquals("5.14", browser.getVersion());
		assertEquals("5", browser.getMajorVersion());
		assertEquals("14", browser.getMinorVersion());
	}

	/**
		 *
		 */
	public void testMSIEWinNT()
	{
		Browser browser = new Browser("Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)");
		assertEquals(Browser.MSIE_BROWSER, browser.getBrowserType());
		assertEquals("5.5", browser.getVersion());
		assertEquals("5", browser.getMajorVersion());
		assertEquals("5", browser.getMinorVersion());
	}

	/**
		 *
		 */
	public void testMozillaLinux()
	{
		Browser browser = new Browser(
				"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.1b) Gecko/20020722");
		assertEquals(Browser.GECKO_BROWSER, browser.getBrowserType());
		assertEquals("20020722", browser.getVersion());
	}

	/**
		 *
		 */
	public void testMozillaMacOSX()
	{
		Browser browser = new Browser(
				"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.0.0) Gecko/20020529");
		assertEquals(Browser.GECKO_BROWSER, browser.getBrowserType());
		assertEquals("20020529", browser.getVersion());
	}

	/**
		 *
		 */
	public void testMozillaWinNT()
	{
		Browser browser = new Browser(
				"Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.0.0) Gecko/20020530");
		assertEquals(Browser.GECKO_BROWSER, browser.getBrowserType());
		assertEquals("20020530", browser.getVersion());
	}

	/**
		 *
		 */
	public void testNetscape4OS9()
	{
		Browser browser = new Browser(
				"Mozilla/4.75C-CCK-MCD {C-UDP; EBM-APPLE} (Macintosh; U; PPC)");
		assertEquals(Browser.NETSCAPE_BROWSER, browser.getBrowserType());
		assertEquals("4.75", browser.getVersion());
		assertEquals("4", browser.getMajorVersion());
		assertEquals("75", browser.getMinorVersion());
	}

	/*
	   public void testMSIEMac()
	   {
	   }
	 */
	public void testNetscape4WinNT()
	{
		Browser browser = new Browser("Mozilla/4.77 [en] (Windows NT 5.0; U)");
		assertEquals(Browser.NETSCAPE_BROWSER, browser.getBrowserType());
		assertEquals("4.77", browser.getVersion());
		assertEquals("4", browser.getMajorVersion());
		assertEquals("77", browser.getMinorVersion());
	}
}
