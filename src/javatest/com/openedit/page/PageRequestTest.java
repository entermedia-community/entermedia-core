/*
 * Created on Nov 13, 2004
 */
package com.openedit.page;

import java.util.List;

import junit.textui.TestRunner;

import com.openedit.BaseTestCase;
import com.openedit.ModuleManager;
import com.openedit.TestFixture;
import com.openedit.WebPageRequest;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageRequestTest extends BaseTestCase
{

	public PageRequestTest( String name )
	{
		super( name );
	}
	public static void main(String[] args)
	{
		TestRunner.run(PageRequestTest.class);
	}

	public void testIsEditable() throws Exception
	{
		TestFixture fixture = getFixture();
		Page page = getPage("/index.html");
		WebPageRequest req = getFixture().createPageRequest("/index.html");
		req.setUser(null);
		assertFalse( req.isEditable() );

	}
	public void testActionCount() throws Exception
	{
		//Page page = getPage("/actions/fouractions.html");
		//List actions = page.getPageActions();
		//assertEquals(3,actions.size());
		
		Page page = getPage("/actions/includesfouractions.html");
		List actions = page.getPageActions();
		assertEquals(2,actions.size()); //some dups in there

		WebPageRequest context = getFixture().createPageRequest("/actions/includesfouractions.html");
		getFixture().getEngine().beginRender(context);
		
		//now check the actions counts again
		page = getPage("/actions/fouractions.html");
		actions = page.getPageActions();
		assertEquals(2,actions.size());
		
	}
/*
	public void testOverrideDirectory() throws Exception
	{
		Page overridepage = getPage("/overrideme/apos.html");
		overridepage = getPage("/overrideme/apos.html");
		assertNotNull( overridepage.getLayout() );
		assertEquals( "/overrideme/layout1.html", overridepage.getLayout());
		String content = overridepage.getContent();
		assertEquals("<DIV>Real File</DIV>",content);
	}
	*/
	public void testFallBackDirectory() throws Exception
	{
		Page overridepage = getPage("/fallbackme/sub/fake.html");
		assertTrue( overridepage.exists() );
		assertNotNull( overridepage.getLayout() );
		assertEquals( "/fallbackme/layout1.html", overridepage.getLayout());
		String content = overridepage.getContent();
		assertEquals("<DIV>Fallback File</DIV>",content);
	}
	public void testFallBackDirectoryUseLayout() throws Exception
	{
		Page overridepage = getPage("/fallbackmenolayout/index.html");
		assertNotNull( overridepage.getLayout() );
		assertEquals( "/fallbackfiles/layout1junk.html", overridepage.getLayout());
	}
	public void testFallBackSubDirectory() throws Exception
	{
		Page overridepage = getPage("/fallbackme/deep/deeper/content.html");
		assertTrue( overridepage.exists() );
		assertNotNull( overridepage.getLayout() );
		assertEquals( "/fallbackme/layout1.html", overridepage.getLayout());
		String content = overridepage.getContent();
		assertEquals("<DIV>Fallback to Here</DIV>",content);
	}
//	public void testFallBackContentFile() throws Exception
//	{
//
//		Page overridepage = getPage("/fallbackme/nocontentfront.html");
//		assertTrue( overridepage.exists() );
//		String content = overridepage.getContent();
//		assertEquals("<DIV>OLD</DIV>",content);
//
//	}
	public void testFallbackProperty() throws Exception
	{
		Page overridepage = getPage("/fallbackme/somecontent.html");
		assertTrue( overridepage.exists() );
		
		String title = overridepage.get("browserTitle");
		assertEquals("Community",title );

	}

//Removed from lack of use	
//	public void testContentFile() throws Exception
//	{
//		final String VIRTUAL_PAGE = "/contenttest/virtualpage.html";
//		WebPageRequest context = getFixture().createPageRequest(VIRTUAL_PAGE);
//		PageStreamer streamer = context.getPageStreamer();
//		streamer.render();
//
//		String content = context.getWriter().toString();
//		assertTrue(content.indexOf("actual content") >= 0);
//		//check that the layout is used
//		assertTrue(content.indexOf("Welcome") >= 0);
//	}
	public void testStream() throws Exception
	{
		final String VIRTUAL_PAGE = "/test/test.html";
		WebPageRequest context = getFixture().createPageRequest(VIRTUAL_PAGE);
		PageStreamer streamer = context.getPageStreamer();
		assertTrue( streamer.doesExist("./side.html") );
		
		streamer.stream("./side.html");
		String content = context.getWriter().toString();
		assertFalse(content.indexOf("not found") >= 0);
		assertTrue(content.indexOf("side") >= 0);
		
	}
	public void testCancelActions() throws Exception
	{
		Page page = getPage("/actions/cancel/threeactions.html");
		List actions = page.getPathActions();		
		assertEquals(3,actions.size());
		PageAction action = (PageAction)actions.get(2); //this is the last entry taken from
		//fallback directory
		String clas = action.getConfig().getAttribute("class");
		assertEquals("java.sql.Date",clas);
		ModuleManager manager = new ModuleManager();
		List small = manager.condenseActions(null,actions);
		assertEquals(1,small.size());
		PageAction action2 = (PageAction)small.get(0); //now it moved up
		String clas2 = action2.getConfig().getAttribute("class");
		assertEquals("java.sql.Date",clas2);
		
	}
	
	public void testInnerInnerLayout() throws Exception
	{
		final String path = "/innerlayouts/contentpage.html";
		WebPageRequest context = getFixture().createPageRequest(path);
		PageStreamer streamer = context.getPageStreamer();
		
		streamer.render();
		String content = context.getWriter().toString();
		content = content.replace("\r", "");
		assertEquals("1\n2\n3\n4\n5",content);
	}
}
