/*
 * Created on Nov 20, 2004
 */
package org.openedit.generators;

import org.openedit.BaseTestCase;
import org.openedit.WebPageRequest;
import org.openedit.page.Page;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class NestedGeneratorTest extends BaseTestCase
{

	public NestedGeneratorTest( String name )
	{
		super( name );
	}
	

	public void testGenerate() throws Exception
	{
		WebPageRequest context = getFixture().createPageRequest( "/generators/nested.html");
		context.putPageValue("variable", "$othervariable");
		context.putPageValue( "othervariable", ", it worked.");
		context.getPageStreamer().render();
		String result = context.getWriter().toString();
		//System.out.println( result );
		//assertEquals( "<p>\nTest stuff , it worked.\n</p>", result );
	}
}
