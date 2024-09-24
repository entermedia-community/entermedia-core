/*
 * Created on Jan 4, 2005
 */
package org.openedit.repository.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openedit.OpenEditException;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;
import org.openedit.util.PathUtilities;
import org.openedit.util.XmlUtil;

/**
 * We need to move more stuff out of the Base Repository into here so that
 * one day we can implement a Macromedia Contribute version of this
 * @author cburkey
 *
 */
public class XmlVersionRepository extends VersionedRepository
{

	protected XmlUtil fieldXmlUtil;
	
	public XmlVersionRepository()
	{
		
	}
	
	public XmlVersionRepository(String inPath, File inRoot)
	{
		setPath( inPath);
		setExternalPath(inRoot.getAbsolutePath());
	}	

	
	protected SAXReader fieldReader;
	/**
	 * @param inPath
	 * @param version
	 * @return
	 */
	protected ContentItem populateRevision(String inPath, String inVersionsDir, Element version) {
		//	<version number="1" author="admin" date="2323525233" type="added">Some message</version>
		
		String rootDir = PathUtilities.extractDirectoryPath(inPath);
		String filename = PathUtilities.extractFileName(inPath);
		String versionPath = rootDir + "/" + inVersionsDir + "/" + version.attributeValue("number") + "~" + filename ;
		
		ContentItem item = createContentItem(versionPath);
		item.setActualPath(versionPath);

		if(!item.exists())
		{
			//check older version format also
			versionPath = rootDir + "/" + inVersionsDir + "/"+ filename + "~" + version.attributeValue("number")  ;
			item = createContentItem(versionPath);
			item.setActualPath(versionPath);
		}
		item.setPath(inPath);

		//We dont need the date here since we use a FileItem that points to the date on the disk
		//This should be changed if we ever have problems with it.
		
		item.setAuthor(version.attributeValue("author"));
		item.setVersion(version.attributeValue("number"));
		String comment = version.getTextTrim();
		item.setMessage(comment);
		item.setType(version.attributeValue("type"));
		return item;
	}
	public List readAll(String inPath, File inMetadata) throws RepositoryException
	{
			if ( !inMetadata.exists())
			{
				return Collections.EMPTY_LIST;
			}
	        Element root = getXmlUtil().getXml(inMetadata, "UTF-8");
			
			List all = new ArrayList();
			String versionsDir = inMetadata.getParentFile().getName();
	        for (Iterator versions = root.elementIterator("version"); versions.hasNext();) {
				Element version = (Element) versions.next();
				ContentItem item = populateRevision(inPath,versionsDir, version);
				all.add(item);
			}
	        Collections.reverse(all); //put oldest on bottom
	        return all;
	}
	
	public void append(ContentItem inRevision, File inMetadata) throws RepositoryException
	{
	        Document document = null;
			if ( !inMetadata.exists() )
			{
				document = DocumentHelper.createDocument();
				Element root = document.addElement("versions");
				//root.addAttribute("path", inRevision.getPath());
			}
			else
			{
		        document = getXmlUtil().getXml(inMetadata, "UTF-8").getDocument();
			}
	        //TODO check that its not in there already
	        Element version = document.getRootElement().addElement("version");
	        version.addAttribute("number",inRevision.getVersion());
	        Date date = inRevision.lastModified();
	        if ( date == null)
	        {
	        	date = new Date();
	        }
	        version.addAttribute("date",String.valueOf( date.getTime()));
	        version.addAttribute("author",inRevision.getAuthor());
	        version.addAttribute("type",inRevision.getType());
	        version.setText(inRevision.getMessage());

	        inMetadata.getParentFile().mkdirs();
	        getXmlUtil().saveXml(document, inMetadata );
	}
	
	/* (non-javadoc)
	 * @see com.einnovation.repository.filesystem.BaseRepository#getContentItems(java.lang.String)
	 */
	public List getVersions(String inPath) throws RepositoryException
	{
		File file = getFile( inPath );
		if (file.isDirectory())
		{
			return Collections.EMPTY_LIST;
		}
		File versionsDirectory = getVersionsDirectory( file );
		if( !versionsDirectory.exists() )
		{
			versionsDirectory.mkdirs();
		}
	
		//checkVersion(file, inPath);

		File metadata = getMetaDataFile( file );
		List knownContentItems = readAll( inPath, metadata );
		return knownContentItems;
	}
	public ContentItem getLastVersion(String inPath)
	{
		File file = getFile( inPath );

		File metadata = getMetaDataFile( file );
		if ( !metadata.exists() )
		{
			return null;
		}

        Element root = getXmlUtil().getXml(metadata, "UTF-8");		
		String versionsDir = metadata.getParentFile().getName();
		List all = root.elements("version");
		if( all.size() == 0)
		{
			return null;
		}
		Element last = (Element)all.get(all.size() - 1);
		ContentItem item = populateRevision(inPath, versionsDir, last);
		return item;
	}

	
	protected void saveVersion( ContentItem inContentItem ) throws RepositoryException
	{
		File file = getFile( inContentItem.getPath() );
		if ( file.isDirectory() )
		{
			return;
		}
		int maxVersionNum = maxVersionNumber( file );

		//check that this is not the same content as last version
		if ( maxVersionNum > 0)
		{
			File oldversion = getVersionFile(file,String.valueOf(maxVersionNum ) );
			if ( oldversion.length() == file.length())
			{
				//these might be the same
				//TODO: Do a string comparison on them
				return;
			}
		}
		
		inContentItem.setVersion( String.valueOf( maxVersionNum + 1 ) );
		
		File newversion = getVersionFile( file, inContentItem.getVersion() );
		try
		{
			newversion.getParentFile().mkdirs();
			if ( file.exists() )
			{
				getFileUtils().copyFiles( file, newversion );
			}
			File metadata = getMetaDataFile( file );
			append( inContentItem, metadata );
		}
		catch( Exception e )
		{
			throw new RepositoryException( "Error saving ContentItem " + inContentItem.getVersion()
					+ " for path " + inContentItem.getPath(), e );
		}
	}
	public XmlUtil getXmlUtil()
	{
		if( fieldXmlUtil == null)
		{
			fieldXmlUtil = new XmlUtil();
		}
		return fieldXmlUtil;
	}
	public void setXmlUtil(XmlUtil inXmlUtil)
	{
		fieldXmlUtil = inXmlUtil;
	}
	public void deleteOldVersions(String inPath) throws RepositoryException
	{
		File file = getFile( inPath );
		if ( file.isDirectory() )
		{
			return;
		}
		File metadata = getMetaDataFile( file );
		List knownContentItems = readAll( inPath, metadata );
		for (Iterator iterator = knownContentItems.iterator(); iterator.hasNext();)
		{
			FileItem item = (FileItem) iterator.next();
			item.getFile().delete();
		}
		metadata.delete();
	}

	@Override
	public ContentItem getVersion(ContentItem inItem, String inVersion) throws RepositoryException {
		File currentfile = getFile( inItem.getPath() );
		
		File oldversion = getVersionFile( currentfile, inVersion);
		FileItem item = new FileItem();
		item.setFile(oldversion);
		item.setPath(inItem.getPath());
		return item;
	}
	public void restoreVersion(ContentItem inCurrent, String inVersion) throws RepositoryException {
		
		saveVersion(inCurrent);

		File currentfile = getFile( inCurrent.getPath() );
		
		File oldversion = getVersionFile( currentfile, inVersion);

		getFileUtils().copyFiles( oldversion, currentfile );
		
		
	}

}
