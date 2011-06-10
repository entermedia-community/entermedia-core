/*
 * Created on Sep 14, 2004
 */
package com.openedit.page;

import java.io.File;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.BaseTestCase;
import com.openedit.WebPageRequest;
import com.openedit.util.OutputFiller;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageMetaDataTest extends BaseTestCase
{

	private static final Log log = LogFactory.getLog(PageMetaDataTest.class);
//	PageSettings siteMetaData;
//	PageSettings defaultMetaData;
//	PageSettings metaData;

	public PageMetaDataTest( String name )
	{
		super( name );
	}

	public void testUpdateMetaData() throws Exception
	{
		removeSiteXconf();
		Page indexPage = getPage( "/metadata/index.html");
		//assertNull( indexPage.getLayout() );
		assertEquals( "default", indexPage.getProperty( "test_property"));
		
		Page indexPage2 = getPage( "/metadata/index.html");
		assertTrue("Should be the same instance" ,indexPage == indexPage2);
		
		//put the starter xconf in there
		copySiteXconf();
		Thread.sleep(100);
		long start = System.currentTimeMillis();
		indexPage = getPage( "/metadata/index.html");
		log.info( "got page in " + (System.currentTimeMillis() - start) + " ms");
		assertEquals( "Hello!", indexPage.getProperty( "test_property"));
		assertEquals( "/default.html", indexPage.getLayout() );
		
		copyIndexXconf();
		Thread.sleep(100);
		start = System.currentTimeMillis();
		indexPage = getPage( "/metadata/index.html");
		log.info( "got page in " + (System.currentTimeMillis() - start) + " ms");
		//log.info( indexPage.getPageSettings().() );
		assertEquals( "Goodbye!", indexPage.getProperty( "test_property"));
		assertEquals( "Open Edit", indexPage.getProperty( "productName"));
		assertEquals( "/indexlayout.html", indexPage.getLayout() );
		
		removeIndexXconf();
		Thread.sleep(100);
		start = System.currentTimeMillis();
		indexPage = getPage( "/metadata/index.html");
		log.info( "got page in " + (System.currentTimeMillis() - start) + " ms");
		assertEquals( "/default.html", indexPage.getLayout() );
		assertEquals( "Hello!", indexPage.getProperty( "test_property"));
		assertEquals( "Open Edit", indexPage.getProperty( "productName"));

		
		removeSiteXconf();
		Thread.sleep(100);
		start = System.currentTimeMillis();
		indexPage = getPage( "/metadata/sub/index.html");
		log.info( "got page in " + (System.currentTimeMillis() - start) + " ms");
		//assertNull( indexPage. );
		assertEquals( "default", indexPage.getProperty( "test_property"));
		assertEquals( "Open Edit", indexPage.getProperty( "productName"));

		
		copySiteXconf();
		Thread.sleep(100);
		start = System.currentTimeMillis();
		indexPage = getPage( "/metadata/sub/index.html");
		log.info( "got page in " + (System.currentTimeMillis() - start) + " ms");
		//assertNull( indexPage. );
		assertEquals( "Hello!", indexPage.getProperty( "test_property"));
		assertEquals( "/default.html", indexPage.getLayout() );

	
	}
	
	protected void copySiteXconf() throws Exception
	{
		File fromFile = new File( getRoot(), "metadata/testxconfs/_site.xconf");
		File toFile = new File(getRoot(), "metadata/_site.xconf");
		OutputFiller fill = new OutputFiller();
		fill.fill( fromFile, toFile );
	}
	
	protected void copyIndexXconf() throws Exception
	{
		File fromFile = new File(getRoot(), "metadata/testxconfs/index.xconf");
		File toFile = new File(getRoot(), "metadata/index.xconf");
		OutputFiller fill = new OutputFiller();
		fill.fill( fromFile, toFile );
	}
	
	protected void removeIndexXconf() throws Exception
	{
		File toFile = new File(getRoot(), "metadata/index.xconf");
		toFile.delete();
	}
	
	protected void removeSiteXconf() throws Exception
	{
		File toFile = new File(getRoot(), "metadata/_site.xconf");
		toFile.delete();
	}

//	public void testGetLayout()
//	{
//		assertEquals( "layoutindex.xconf" , metaData.getLayout() );
//		metaData.setFallBack( defaultMetaData );
//		assertEquals( "layoutindex.xconf" , metaData.getLayout() );
//		metaData.setLayout(null);
//		metaData.setFallBack( defaultMetaData );
//		assertEquals( "layout_default.xconf" , metaData.getLayout() );
//	}

	public void testSiteLayout() throws Exception
	{
		Page page = getPage("/metadatalayout/sublayout/index.html");
		assertEquals("/defaulttemplate.html",page.getLayout());
	}
	
//	public PageSettings createDateMetaData( String inPath, String inDateString ) throws ParseException
//	{
//		PageSettings metaData = new PageSettings();
//		metaData.setXConf( new DateStampContentItem( inDateString ) );
//		
//		metaData.setLayout( "layout" + inPath );
//		return metaData;
//	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
//		siteMetaData = createDateMetaData( "_site.xconf", "1/10/04");
//		defaultMetaData = createDateMetaData( "_default.xconf", "1/5/04");
//		metaData = createDateMetaData( "index.xconf", "1/1/04");
	}
	
	protected void tearDown() throws Exception
	{
		removeSiteXconf();
		removeIndexXconf();
	}
	
	public void testEncoding() throws Exception
	{
		Page index = getPage("/metadata/testxconfs/index.html");
		String encoding = index.getCharacterEncoding();
		assertEquals( "ISO-8859-1", encoding );
		
		WebPageRequest req = getFixture().createPageRequest("/metadata/testxconfs/index.html");
		getFixture().getEngine().beginRender(req);
		//req.getWriter().toString();
		encoding = req.getPage().getCharacterEncoding();
		assertEquals( "ISO-8859-1", encoding );
		
		
		
	}
}
