/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*
 * Created on May 8, 2003
 *
 */
package org.openedit.page.finder;

import org.openedit.BaseTestCase;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;


/**
 * DOCUMENT ME!
 *
 * @author cburkey
 */
public class InfiniteLoopTest extends BaseTestCase
{

	/**
	 * Constructor for InfiniteLoopTest.
	 *
	 * @param arg0
	 */
	public InfiniteLoopTest(String arg0)
	{
		super(arg0);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Throwable
	 */
	public void testLoadingATemplate() throws Throwable
	{
		String path = "/normal.html";

		loadPage(path);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Throwable
	 */
	public void XXXtestLoop() throws Throwable
	{
		String path = "/selfreferingtest1.html";
		boolean exceptionThrown = false;
		try
		{
			loadPage(path);
		}
		catch (OpenEditException e)
		{
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Throwable
	 */
	public void testNormalTemplatedPage() throws Throwable
	{
		String path = "/defaulttemplate.html";

		loadPage(path);
	}

	protected void loadPage(String path) throws Exception, OpenEditException, Throwable
	{
		WebPageRequest context = getFixture().createPageRequest(path);
		
		getFixture().getEngine().beginRender(context);
		String pageContents = context.getWriter().toString();
		assertTrue(pageContents.length() < 100000); //since its infinite it would be blank or a short error
	}
}
