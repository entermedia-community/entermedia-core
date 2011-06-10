/*
 * Created on Nov 20, 2004
 */
package com.openedit.generators;

import com.openedit.BaseTestCase;
import com.openedit.WebPageRequest;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class FileGeneratorTest extends BaseTestCase
{

	public FileGeneratorTest( String name )
	{
		super( name );
	}

	public void testGenerate() throws Exception
	{
		WebPageRequest context = getFixture().createPageRequest("generators/file.html");
		context.getPageStreamer().render();
		String result = context.getWriter().toString();
		assertTrue(result.contains("$variable") );
		
	}
}
