/*
 * Created on Nov 20, 2004
 */
package org.openedit.generators;

import org.openedit.BaseTestCase;
import org.openedit.WebPageRequest;

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
