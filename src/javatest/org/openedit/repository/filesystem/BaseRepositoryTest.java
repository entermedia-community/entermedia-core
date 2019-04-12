/*
 * Created on Aug 7, 2004
 */
package org.openedit.repository.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import org.openedit.repository.ContentItem;
import org.openedit.repository.Repository;
import org.openedit.repository.RepositoryException;
import org.openedit.repository.filesystem.DirectoryTool;
import org.openedit.repository.filesystem.FileItem;
import org.openedit.repository.filesystem.StringItem;
import org.openedit.repository.filesystem.VersionedRepository;
import org.openedit.repository.filesystem.XmlVersionRepository;
import org.openedit.util.OutputFiller;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class BaseRepositoryTest extends TestCase
{
	protected VersionedRepository fieldRepository;
	protected DirectoryTool fieldDirectoryTool;


	public BaseRepositoryTest( String arg0 )
	{
		super( arg0 );
	}

	public DirectoryTool getDirectoryTool()
	{
		if (fieldDirectoryTool == null)
		{
			fieldDirectoryTool = new DirectoryTool();
		}
		return fieldDirectoryTool;
	}

	public VersionedRepository getRepository()
	{
		if (fieldRepository == null)
		{
			fieldRepository = new XmlVersionRepository();
			fieldRepository.setExternalPath(getRootDirectory().getAbsolutePath().replace('\\', '/'));
		}
		return fieldRepository;
	}
	
	public void testGet_NoFile() throws Exception
	{
		ContentItem contentItem = getRepository().get( "/index.html");
		assertNotNull( contentItem );
		//assertEquals( "1", contentItem.getVersion() );
		assertEquals( "index.html",((FileItem)contentItem).getFile().getName() );
		assertFalse( contentItem.exists() );
		System.out.println( contentItem.getMessage() );
		
		makeIndexFile();
		Thread.sleep(2000);
		contentItem = getRepository().get( "/index.html" );
	//	assertEquals( "1", contentItem.getVersion() );
	//  Should the version number increment?
	//	assertEquals( "2", contentItem.getVersion() );
		assertTrue( contentItem.exists() );
		checkContentItemContent( contentItem );
		System.out.println( contentItem.getMessage() );
		
	}
	
	public void checkContentItemContent( ContentItem inContentItem ) throws Exception
	{
		InputStreamReader reader = new InputStreamReader(inContentItem.getInputStream());
		char[] buffer = new char[512];
		int charsRead = reader.read(buffer);
		assertTrue( charsRead > 0 );
		String content = new String( buffer,0, charsRead);
		System.out.println( content );
		assertEquals( "<p>This is a blank page</p>", content );
		reader.close();
	}
	
	public void testGet_FileExists() throws Exception
	{
		putIndexFile();
		ContentItem contentItem = getRepository().get( "/index.html");
		assertNotNull( contentItem );
		//assertEquals( "1", contentItem.getVersion() );
		assertEquals( "index.html",((FileItem)contentItem).getFile().getName() );
		assertTrue( contentItem.exists() );
		
		// If we touch the file, the repository should make a new contentItem
		makeIndexFile();
		contentItem = getRepository().get( "/index.html");
		assertNotNull( contentItem );
		//assertEquals( "1", contentItem.getVersion() );
		assertEquals( "index.html",((FileItem)contentItem).getFile().getName() );
		assertTrue( contentItem.exists() );
	}
	
	public void testGet_Folder() throws Exception
	{
		putIndexFile();
		ContentItem root = getRepository().get( "/");
		assertNotNull( root );
		//assertEquals( "1", root.getVersion() );
		assertEquals( "/",root.getPath() );
		assertTrue( root.exists() );
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputFiller filler = new OutputFiller();
		filler.fill( root.getInputStream(), out );
		root.getInputStream().close();
		assertEquals( "index.html\n", out.toString() );
	}
	
	public void testGetAndPut() throws Exception
	{
		makeIndexFile();
		ContentItem contentItem = getRepository().get( "/index.html" );
		checkContentItemContent( contentItem );
		//assertEquals( "1", contentItem.getVersion() );
		contentItem = getRepository().get( "/index.html" );
		getRepository().put( contentItem );  //Save to itself
		contentItem = getRepository().get( "/index.html" );
		checkContentItemContent( contentItem );
		//assertEquals( "2", contentItem.getVersion() );	
	}
	
	public void testGetLatestContentItem() throws Exception
	{
		makeIndexFile();
		putIndexFile();
		List items = getRepository().getVersions("/index.html");
		ContentItem contentItem = (ContentItem)items.get(0);
		assertEquals("1", contentItem.getVersion() );
		assertEquals( "1~index.html",((FileItem)contentItem).getFile().getName() );
		assertTrue( contentItem.exists() );
	}

	public void testPut() throws Exception
	{
		File indexFile = makeIndexFile();
		assertEquals( 0, getRepository().maxVersionNumber( indexFile ) );
		putIndexFile();
		assertEquals( 1, getRepository().maxVersionNumber( indexFile ) );
		putIndexFile();
		putIndexFile();
		putIndexFile();
		//It had not changed so the version should still be one
		assertEquals( 1, getRepository().maxVersionNumber( indexFile ) );
		
		StringItem item = new StringItem( "/index.html",  "<p>New content XXX</p>" ,null);
		item.setMessage("Automatic ContentItem from filesystem." );
		item.setAuthor("admin");
		getRepository().put(item);
		assertEquals( 2, getRepository().maxVersionNumber( indexFile ) );
		

		
	}


	public void testCheckReservedPaths() throws Exception
	{
		assertBadPath( "/foo/bar/.versions" );
		assertBadPath( "/foo/bar/.versions/.somestuff" );
		try
		{
			getRepository().checkReservedPaths( "/foo/bar/index.html" );
		}
		catch( RepositoryException e )
		{
			assertTrue( false );
		}
	}
	protected void assertBadPath( String inPath ) throws Exception
	{
		try
		{
			getRepository().checkReservedPaths( inPath );
			assertTrue( false );
		}
		catch( RepositoryException e )
		{
			assertTrue( true );
		}
	}

	/**
	 * This method should be the exact number of version on file
	 * @throws Exception
	 */
	public void testMaxVersionNumber() throws Exception
	{
		File indexFile = makeIndexFile();
		assertEquals( 0, getRepository().maxVersionNumber( indexFile ) );
		putIndexFile();
		assertEquals( 1, getRepository().maxVersionNumber( indexFile ) );
	}


	public void testRemove() throws Exception
	{
		putIndexFile();
		ContentItem contentItem = getRepository().get("/index.html");
		assertTrue( contentItem.exists() );
		getRepository().remove( contentItem );
		assertTrue( !contentItem.exists() );
	}


	public void testgetContentItems() throws Exception
	{
		getRepository();
		//original
		File indexFile = makeIndexFile(); //original file
		
		assertTrue ( indexFile.exists() );
		
		//added a file
		//putIndexFile();  //created a version
		
		List contentItems = getRepository().getVersions( "/index.html" );
		assertEquals( 1, contentItems.size() );
		ContentItem automaticContentItem = (ContentItem) contentItems.get(0); 
		assertFirstContentItem( automaticContentItem );
		
		ContentItem contentItem = new StringItem("/foo/bar/file.html", "<p>New content</p>" ,null);
		getRepository().put( contentItem );
		contentItems = getRepository().getVersions( "/foo/bar" );
		assertEquals( 0, contentItems.size() );
	}
	
	public void testGetContentItemForExternalEdits() throws Exception
	{
		//original
		File indexFile = makeIndexFile();
		
		assertTrue ( indexFile.exists() );
		List contentItems = getRepository().getVersions( "/index.html" );
		assertEquals( 1, contentItems.size() );
		//assertFirstContentItem( automaticContentItem );

		//added the second rev
		putIndexFile();
		
		contentItems = getRepository().getVersions( "/index.html" );
		assertEquals( 2, contentItems.size() );
	}
	
	protected void assertFirstContentItem( ContentItem inContentItem )
	{
		assertEquals( "admin", inContentItem.getAuthor() );
		assertEquals( "automatic version", inContentItem.getMessage() );
		assertEquals( "1", inContentItem.getVersion() );
		assertEquals( "/index.html", inContentItem.getPath() );
		assertEquals( ContentItem.TYPE_ADDED, inContentItem.getType() );
		assertNotNull( inContentItem.lastModified() );
	}
	
	public File makeIndexFile() throws Exception
	{
		File indexFile = new File( getRepository().getExternalPath(), "index.html" );
		StringReader reader = new StringReader("<p>This is a blank page</p>" );
		Writer writer = new FileWriter( indexFile );
		OutputFiller filler = new OutputFiller();
		filler.fill( reader, writer );
		writer.flush();
		writer.close();

		File versionsdir = new File( getRepository().getExternalPath(), ".versions" );
		versionsdir.mkdirs();
		return indexFile;
	}
	
	public void putIndexFile() throws Exception
	{
		//ContentItem contentItem = getRepository().get("/index.html");
		//assertNotNull(contentItem);
		StringItem item = new StringItem( "/index.html",  "<p>New content</p>" ,null);
		item.setMessage("junit test" );
		item.setAuthor("admin");
		getRepository().put(item);
	}


	public void testGetVersionsDirectory() throws Exception
	{
		getRepository();
		makeIndexFile();
		
		File versionsDirectory = getRepository().getVersionsDirectory( "/foo/bar/file.html" );
		assertFalse( versionsDirectory.exists() );
		
		
		ContentItem contentItem = new StringItem("/foo/bar/file.html", "<p>New content</p>",null );
		getRepository().put( contentItem );
		versionsDirectory = getRepository().getVersionsDirectory( "/foo/bar/file.html" );
		assertTrue( versionsDirectory.exists() );

	}
	public File getRootDirectory()
	{
		return getDirectoryTool().getRootDirectory();
	}
	protected void tearDown() throws Exception
	{
		getDirectoryTool().tearDown();

	}

}
