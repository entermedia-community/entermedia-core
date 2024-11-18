/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.page;

import java.io.File;
import java.util.Collection;

import org.openedit.BaseTestCase;
import org.openedit.repository.filesystem.FileItem;
import org.openedit.util.strainer.AndFilter;
import org.openedit.util.strainer.Filter;
import org.openedit.util.strainer.FilterReader;
import org.openedit.util.strainer.GroupFilter;
import org.openedit.util.strainer.NotFilter;
import org.openedit.util.strainer.OrFilter;
import org.openedit.util.strainer.PermissionFilter;
import org.openedit.util.strainer.UserFilter;


/**
 * Test for {@link DynamicPage}
 *
 * @author Eric Galluzzo
 */
public class PageTest extends BaseTestCase
{
	public static final String PATH_WITH_FILTERS = "withfilters.html";

	public PageTest(String inName)
	{
		super(inName);
	}

	public void testMixedFallBackAlternativeContent() throws Exception
	{
		Page pagenormal = getPage( "/realfiles/inbase.html" );
		assertTrue("inbase does not exist",pagenormal.exists());
		String id = pagenormal.getId();
		System.out.println(id);
	}

	public void testSimpleXconf() throws Exception
	{
		//The rules
		
		//1. PageSettings may be blank and have only one Path
		//2. We should use the PageSettings.setAlternativePath to show the real path
		
		Page page = getPage( "/simple/test.html" );
		assertEquals("/simple/test.xconf",page.getPageSettings().getPath());
		PageProperty prop = page.getPageSettings().getProperty("simple");
		assertNotNull("Prop should be set",prop);
		assertEquals("true",prop.getValue());
		
//		PageSettings  settings = getFixture().getPageManager().getPageSettingsManager().getPageSettings("/simple/test.html");
//		
//		assertEquals("/WEB-INF/base/simple/test.html",settings.getXConf().getActualPath());
	}

/*
	public void testFallBackRedirected() throws Exception
	{
		//a falls back to a_fallback
		//b falls back to b_fallback
		
		Page page = getFixture().getPageManager().getPage("/fallbackredirect_modules/asset/editor/test.html");
		String path = page.getPageSettings().getFallback().getPath();
		//assertEquals("/fallbackredirect_modules/default/editor/test.xconf", path);
		assertEquals("/WEB-INF/base/fallbackredirect_modules/asset/editor/test.xconf", path);
		
		
		assertEquals("Correct!", page.getProperty("title"));
	}
*/
	public void testFallBackChain() throws Exception
	{
		Page pagemissing = getPage( "/missingfile/sub/index.html" );
		assertEquals("/WEB-INF/base/missingfile/sub/index.xconf",pagemissing.getPageSettings().getFallback().getPath());
		
		
		Page page = getPage( "/fallbackchain1/sub/index.html" );
		assertEquals("/fallbackchain1/sub/index.xconf",page.getPageSettings().getPath());
		assertEquals("/fallbackchain2/sub/index.xconf",page.getPageSettings().getFallback().getPath());
		
		assertEquals("/fallbackchain1/sub/_site.xconf",page.getPageSettings().getParent().getPath());
		assertEquals("/fallbackchain2/sub/_site.xconf",page.getPageSettings().getParent().getFallback().getPath());
		assertEquals("/fallbackchain3/sub/_site.xconf",page.getPageSettings().getParent().getFallback().getFallback().getPath());
		assertEquals("/WEB-INF/base/fallbackchain3/sub/_site.xconf",page.getPageSettings().getParent().getFallback().getFallback().getFallback().getPath());
		assertEquals("/WEB-INF/base/fallbackchain4/sub/_site.xconf",page.getPageSettings().getParent().getFallback().getFallback().getFallback().getFallback().getPath());
		assertNull(page.getPageSettings().getParent().getFallback().getFallback().getFallback().getFallback().getFallback());

		
		assertEquals("level1", page.get("level1"));
		assertEquals("level2", page.get("level2"));
		assertEquals("level3", page.get("level3"));
		
		page = getPage( "/fallbackchain1/sub/inlevel2.html" );
		assertTrue( page.exists());

		page = getPage( "/fallbackchain1/sub/inlevel3.html" );
		assertTrue( page.exists());

		page = getPage( "/fallbackchain1/sub/inbasefallbackchain3.html" );
		assertTrue( page.exists());

		page = getPage( "/fallbackchain1/sub/inbasefallbackchain4.html" );
		assertTrue( page.exists());

		assertTrue( page.isCurrent() );
		assertTrue( page.isCurrent() );
		assertTrue( page.isCurrent() );
		assertTrue( page.isCurrent() );
	}

	public void testShadowFallBack() throws Exception
	{
		Page page = getPage( "/appwithshadow/AToB/inb.html" );
		assertEquals("/appwithshadow/AToB/inb.xconf",page.getPageSettings().getPath());
		assertEquals("/appwithshadow/AToB/_site.xconf",page.getPageSettings().getParent().getPath());
		
//		PageSettings B = page.getPageSettings().getFallback();
//		assertEquals("/appwithshadow/B/inb.xconf",B.getPath());
//		assertEquals("/appwithshadow/B/_site.xconf",B.getParent().getPath());
//
//		assertEquals("/WEB-INF/base/ShadowFallBack/B/inb.xconf",B.getFallback().getPath());
//		assertEquals("/WEB-INF/base/ShadowFallBack/B/_site.xconf",B.getFallback().getParent().getPath());
		assertTrue(page.exists());
		
	}
	/*
	public void testShadowFallBack2() throws Exception
	{
		Page page = getPage( "/appwithshadow2/AToB/inb.html" );
		
		PageSettings AToB = page.getPageSettings();
		
		assertEquals("/appwithshadow2/AToB/_site.xconf",AToB.getParent().getPath());  
		assertEquals("/appwithshadow2/_site.xconf",AToB.getParent().getParent().getPath());

		PageProperty prop =  AToB.getProperty("fallbackdirectory");
		assertNotNull("Property should exist",prop);
		assertEquals("/appwithshadow2/_site.xconf",prop.getPath());

		PageProperty appid =  AToB.getProperty("applicationid");
		assertNotNull("Property should exist",appid);
		assertEquals("appwithshadow2",appid.getValue());

	//	assertEquals("/WEB-INF/base/ShadowFallBack/B/_site.xconf",B.getPath());
		
		assertEquals("/appwithshadow2/AToB/inb.xconf",AToB.getPath());
		assertEquals("/appwithshadow2/AToB/_site.xconf",AToB.getParent().getPath());

		PageSettings fallback = getFixture().getPageManager().getPageSettingsManager().getPageSettings("/WEB-INF/base/ShadowFallBack2/AToB/_site.xconf");
		PageProperty baseprop =  fallback.getProperty("fallbackdirectory");
		assertNotNull("Property should exist",baseprop);
		assertEquals("/WEB-INF/base/ShadowFallBack2/AToB/_site.xconf",baseprop.getPath());
		
		
//		assertEquals("/WEB-INF/base/ShadowFallBack/AToB/_site.xconf",AToB.load);
//
//		PageSettings B = AToB.getParent().getFallback().getFallback();
//		assertNotNull("Should be auto set relative to the original url",B);
//		assertEquals("/WEB-INF/base/ShadowFallBack/B/_site.xconf",B.getPath());
		
//		assertEquals("/appwithshadow2/B/inb.xconf",B.getPath());
//		assertEquals("/appwithshadow2/B/_site.xconf",B.getParent().getPath());
//
//		assertEquals("/WEB-INF/base/ShadowFallBack/B/inb.xconf",B.getFallback().getPath());
//		assertEquals("/WEB-INF/base/ShadowFallBack/B/_site.xconf",B.getFallback().getParent().getPath());
		assertTrue(page.exists());
		
	}
	*/
	
	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testLoadConfiguration_Filters() throws Exception
	{
		Page pageWithFilters = getPage(PATH_WITH_FILTERS);
		checkFilter(pageWithFilters.getPermission("view").getRootFilter());
		checkFilter(pageWithFilters.getPermission("edit").getRootFilter());
	}
	
	public void testIsBinary() throws Exception
	{
		Page indexPage = getPage( "index.html" );
		Page jpgPage = getPage( "picture.jpg" );
		assertFalse( indexPage.isBinary() );
		System.out.println( jpgPage.getMimeType() );
		assertTrue( jpgPage.isBinary() );
	}

	protected void checkFilter(Filter inFilter)
	{
		assertTrue("Outer filter should be an AndFilter", inFilter instanceof AndFilter);

		AndFilter andFilter = (AndFilter) inFilter;
		assertEquals("Number of AndFilter children", 3, andFilter.getFilters().length);

		assertTrue(
			"First AndFilter child should be a UserFilter",
			andFilter.getFilters()[0] instanceof UserFilter);

		UserFilter userFilter1 = (UserFilter) andFilter.getFilters()[0];
		assertNull("AndFilter's UserFilter's username should be null", userFilter1.getUsername());

		assertTrue(
			"Second AndFilter child should be an OrFilter",
			andFilter.getFilters()[1] instanceof OrFilter);

		OrFilter orFilter = (OrFilter) andFilter.getFilters()[1];
		assertEquals("Number of OrFilter children", 2, orFilter.getFilters().length);

		assertTrue(
			"First OrFilter child should be a GroupFilter",
			orFilter.getFilters()[0] instanceof GroupFilter);

		GroupFilter groupFilter = (GroupFilter) orFilter.getFilters()[0];
		assertEquals("foo", groupFilter.getGroupId());

		assertTrue(
			"Second OrFilter child should be a PermissionFilter",
			orFilter.getFilters()[1] instanceof PermissionFilter);

		PermissionFilter permissionFilter = (PermissionFilter) orFilter.getFilters()[1];
		assertEquals("bar", permissionFilter.getPermission());

		assertTrue(
			"Third AndFilter child should be a NotFilter",
			andFilter.getFilters()[2] instanceof NotFilter);

		NotFilter notFilter = (NotFilter) andFilter.getFilters()[2];
		assertTrue(
			"NotFilter's child should be a UserFilter", notFilter.getFilter() instanceof UserFilter);

		UserFilter userFilter2 = (UserFilter) notFilter.getFilter();
		assertEquals("NotFilter's UserFilter's username", "baz", userFilter2.getUsername());

	
		Filter copy =userFilter2.copy((FilterReader)getFixture().getModuleManager().getBean("filterReader"),"somename");
		assertNotNull(copy);
		assertTrue( copy instanceof UserFilter);
		assertEquals("NotFilter's UserFilter's username", "baz", ((UserFilter)copy).getUsername());
	
	}

	public void testNoLayout() throws Exception
	{
		Page page = getPage( "layout_tests/no_layout.html" );
		assertTrue( page.exists() );
		assertFalse( page.hasLayout() );
	}

	public void testRelativeLayout() throws Exception
	{
		Page page = getPage( "layout_tests/relative_layout.html" );
		assertTrue( page.exists() );
		
		Page layoutPage = getPage(page.getLayout());
		assertTrue( layoutPage.exists() );
		assertEquals( "/layout_tests/layout2.html", layoutPage.getPath() );
	}

	public void testNullLayout() throws Exception
	{
		Page page = getPage( "/layout_tests/null_layout.html" );
		assertTrue( page.exists() );
		
		
		Page altpage = getPage( "/WEB-INF/base/layout_tests/nonexistent.html" );
		//assertEquals( null,altpage.getAlternateContentPath());
		assertFalse( altpage.exists() );

		
		Page laypage = getPage( "/layout_tests/nonexistent.html" );
		assertFalse( laypage.exists() );

		Page layoutPage = getPage(page.getLayout());
		FileItem item = (FileItem)layoutPage.getContentItem();
		String path = item.getFile().getPath();
		assertTrue(item.getFile().getPath(),path.endsWith( File.separator + "layout_tests" + File.separator + "nonexistent.html") );
		assertTrue( !layoutPage.exists() );
	}

	public void testNestedLayout() throws Exception
	{
		Page page = getPage( "layout_tests/nested_layout.html" );
		assertTrue( page.exists() );
		Page layoutPage = getPage(page.getLayout());
		assertTrue( layoutPage.exists() );
		assertEquals( "/layout_tests/layout3.html", layoutPage.getPath() );
	}
	public void testConfigList() throws Exception
	{
		Page page = getPage( "/fallbackme/index.html" );
		PageSettings back = page.getPageSettings().getParent();
		assertNotNull(back);
		assertTrue(back.getPath().endsWith("/fallbackme/_site.xconf"));
		back = (PageSettings)back.getParent();
		assertTrue(back.getPath().endsWith("/_site.xconf"));
		
		PageSettings fback = page.getPageSettings().getFallback();
		assertNotNull(fback);
		assertTrue(fback.getPath().endsWith("/fallbackfiles/index.xconf"));
		fback = fback.getParent();
		assertTrue(fback.getPath().endsWith("/fallbackfiles/_site.xconf"));
		fback = fback.getParent();
		assertTrue(fback.getPath().endsWith("/_site.xconf"));
	}
	
	public void testBaseWebInfFallBack() throws Exception
	{
		Page page = getPage( "/autofallback/sub/index.html" );
		assertTrue(page.getPageSettings().getPath().endsWith("/autofallback/sub/index.xconf"));
		PageSettings back = page.getPageSettings().getParent();
		assertNotNull(back);
		assertTrue(back.getPath().endsWith("/autofallback/sub/_site.xconf"));
		back = (PageSettings)back.getParent();
		assertTrue(back.getPath().endsWith("/autofallback/_site.xconf"));
		back = (PageSettings)back.getParent();
		assertTrue(back.getPath().endsWith("/_site.xconf"));

		
		PageSettings fback = page.getPageSettings().getFallback();
		assertNotNull(fback);
		assertTrue(fback.getPath().endsWith("/WEB-INF/base/autofallback/sub/index.xconf"));
		fback = fback.getParent();
		assertTrue(fback.getPath().endsWith("/WEB-INF/base/autofallback/sub/_site.xconf"));
		fback = fback.getParent();
		assertTrue(fback.getPath().endsWith("/WEB-INF/base/autofallback/_site.xconf"));
		fback = fback.getParent();
		assertTrue(fback.getPath().endsWith("/WEB-INF/base/_site.xconf"));
		
//		String il = page.getPageSettings().getInnerLayout();
//		assertEquals("/autofallback/sub/layout0.html", il);
//		Page ilpage = getPage(il);
//		String ol = ilpage.getPageSettings().getInnerLayout();
//		//assertEquals("/layout1.html", ol);
	
		String il = page.getInnerLayout();
		assertEquals("/autofallback/sub/layout0.html", il);
		Page ilpage = getPage(il);
		String ol = ilpage.getInnerLayout();
		assertEquals("/layout1.html", ol);
		
	}

	public void testAlternativeContent() throws Exception
	{
		Page pagenormal = getPage( "/autofallback/sub/index.html" );
		assertTrue(pagenormal.exists());
		assertEquals( "/autofallback/sub/index.html",pagenormal.getPath());
		assertEquals( "/WEB-INF/base/autofallback/sub/index.html",pagenormal.getAlternateContentPath());
		assertTrue( pagenormal.exists());

		
		Page page = getPage( "/autofallback/_site.xconf" );
		assertEquals( "/autofallback/_site.xconf",page.getPath());
		//assertEquals( "/WEB-INF/base/autofallback/_site.xconf",page.getAlternateContentPath());
		assertTrue( page.getPageSettings().getFallback().exists());
		
	}

	
}
