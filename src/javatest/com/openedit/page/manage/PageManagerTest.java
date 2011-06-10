/*
 * Created on Aug 20, 2004
 */
package com.openedit.page.manage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.openedit.repository.InputStreamItem;

import com.openedit.BaseTestCase;
import com.openedit.page.Page;
import com.openedit.util.OutputFiller;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageManagerTest extends BaseTestCase
{
	
	public PageManagerTest( String arg0 )
	{
		super( arg0 );
		// TODO Auto-generated constructor stub
	}
	
	protected PageManager getPageManager() throws Exception
	{
		return getFixture().getPageManager();
	}

	public void testPageManager()
	{
	}

	public void testGetPage() throws Exception
	{
		Page page = getPageManager().getPage( "/normal.html" );
		assertNotNull( page );
		assertNotNull( page.getPageSettings() );
		assertTrue( page.exists() );
	}
	
	public void testGetPage_xml() throws Exception
	{
		Page page = getPageManager().getPage( "/withconfig.xml" );
		assertNotNull( page );
		assertNotNull( page.getPageSettings() );
		assertTrue( page.exists() );
		
		// Should this be true?  
		// The javadoc says it should be, but is this still the desired behavior?
		Page htmlpage = getPageManager().getPage( "/withconfig.html" );
		assertTrue( htmlpage.exists() );
	}
	
	public void testGetPage_png() throws Exception
	{
		Page page = getPageManager().getPage( "/withconfig.xml"  );
		assertNotNull( page );
		assertNotNull( page.getPageSettings() );
		assertTrue( page.exists() );
		
		// This should definitely not be true
		Page pngpage = getPageManager().getPage( "/withconfig.png" );
		assertTrue( !pngpage.exists() );
	}

	public void testGetPage_NotFound() throws Exception
	{
		Page notfound = getPageManager().getPage( "/nothere.html" );
		assertNotNull( notfound );
		assertFalse( notfound.exists() );
	}
	
	public void testPutPage_FileUpload() throws Exception
	{
		Page newPage = getPageManager().getPage( "/teststuff.html");
		
		InputStreamItem uploadItem = new InputStreamItem();
		uploadItem.setInputStream( new ByteArrayInputStream( "Testing stuff".getBytes() ) );
		uploadItem.setPath( newPage.getPath() );
		newPage.setContentItem( uploadItem );
		getPageManager().putPage( newPage );
		
		// Make sure we can get valid content back!  
		// We can't just leave the InputStreamItem in there because it's stream is exhausted.
		newPage = getPageManager().getPage( "/teststuff.html" );
		OutputFiller filler = new OutputFiller();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		filler.fill( newPage.getInputStream(), out );
		assertEquals( "Testing stuff", out.toString() );
	}
	
	
}
