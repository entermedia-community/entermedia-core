/*
 * Created on Nov 15, 2004
 */
package com.openedit.servlet;

import com.openedit.BaseTestCase;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;
import com.openedit.page.PageRequestKeys;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class OpenEditEngineTest extends BaseTestCase
{

	public OpenEditEngineTest( String name )
	{
		super( name );
	}

	public void testExecutePageActions() throws Exception
	{
		WebPageRequest pageRequest = getFixture().createPageRequest("/actions/pagevalue.html");
		
		pageRequest.removePageValue( PageRequestKeys.USER );
		  
		//FakeServletContext servletContext = new FakeServletContext();
		//servletContext.setAttribute( "PageValue", new PageValueModule() );

		getEngine().beginRender( pageRequest );
		String result = pageRequest.getWriter().toString();
		//System.out.println(result);
		assertNotNull(result);
		assertTrue( result.indexOf( "$date2" ) < 0 );
		
		Page page = getPage( "/actions/springpagevalue.html" );

		pageRequest.getPageStreamer().stream(page);
		
		result = pageRequest.getWriter().toString();
		//System.out.println(result);
		assertTrue( result.indexOf( "$date2" ) < 0 );
		assertTrue( result.indexOf( "days" )  > 0 );
	
	}
	
	public void testRender() throws Exception
	{
		Page page = getPage( "normal.html" );
		WebPageRequest pageRequest = getFixture().createPageRequest("normal.html");
		//Remove the user, otherwise you could get a page decoration
		pageRequest.removePageValue( PageRequestKeys.USER );
		pageRequest.getPageStreamer().include(page);
		String result = pageRequest.getWriter().toString();
		//System.out.println(result);
		result = result.replace("\r", "");
		assertEquals(  result ,"<p>\nTest stuff $variable\n</p>" );
	}
	
	protected OpenEditEngine getEngine()
	{
		return getFixture().getEngine();
	}
}
 