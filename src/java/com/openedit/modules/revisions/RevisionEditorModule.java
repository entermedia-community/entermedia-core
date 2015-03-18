/*
 * Created on Dec 30, 2004
 */
package com.openedit.modules.revisions;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.modules.edit.BaseEditorModule;
import com.openedit.page.Page;
import com.openedit.users.User;

/**
 * A module containing commands required by the revision editor (repository
 * history browser).
 * 
 * @author Eric Galluzzo, egalluzzo@einnovation.com
 */
public class RevisionEditorModule extends BaseEditorModule
{
	private static Log log = LogFactory.getLog( RevisionEditorModule.class );

	/**
	 * Places all the revisions for the path denoted by the "path" request
	 * parameter in the session as attribute "revisions".
	 * 
	 * @param inContext  The web page context
	 */
	public RevisionSession getRevisions( WebPageRequest inContext ) throws OpenEditException
	{
		String path = inContext.getRequestParameter( "editPath" );
		if ( path == null)
		{
			return null;
		}
		RevisionSession session = (RevisionSession)inContext.getPageValue("revisions");
		if ( session == null || !session.getEditPath().equals(path))
		{		
			Page editPage = getPageManager().getPage(path);
			session = new RevisionSession();
			session.setEditPage(editPage);
			session.setOriginalUrl(inContext.getRequestParameter("origURL"));
			session.setParentName(inContext.getRequestParameter("parentName"));
		}
		List revisions = getPageManager().getRepository().getVersions( path );
		session.setRevisions(revisions);
		inContext.putSessionValue( "revisions", session );
		inContext.putPageValue( "revisions", session );

		return session;
	}
	
	public ContentItem getLatestRevision(WebPageRequest inReq) throws RepositoryException
	{
		String path = inReq.getRequestParameter( "editPath" );
		if ( path == null)
		{
			return null;
		}
		ContentItem revision = getPageManager().getLatestVersion( path );
		if (revision != null )
		{
			inReq.putPageValue("latest", revision);
			
			String author = revision.getAuthor();
			String catalogid = inReq.findValue("catalogid");
			
			User user = (User) getSearcherManager().getData(catalogid, "user", author);
			if (user != null)
			{
				inReq.putPageValue("latestemail", user.getEmail());
			}
			return revision;
		}
		return null;
	}

	/**
	 * Retrieves the content item for a revision.  The <code>version</code>
	 * parameter must be set to the version number to retrieve, and the
	 * <code>path</code> parameter to the path to retrieve.  The
	 * {@link Revision} corresponding to that version of that file will be
	 * placed in the session attribute <code>revision</code>, and the content
	 * of that revision in the session attribute <code>revisionContent</code>,
	 * if it is non-binary.  This method assumes that there is already a list
	 * of {@link Revision}s in the session under the attribute name
	 * <code>revisions</code>.
	 * 
	 * @param inContext  The web page context
	 */
	public ContentItem getRevisionContent( WebPageRequest inContext )
		throws OpenEditException
	{
		RevisionSession session = getRevisions(inContext);
		String version = inContext.getRequiredParameter( "version" );

		for ( Iterator iter = session.getRevisions().iterator(); iter.hasNext(); )
		{
			ContentItem revision = (ContentItem) iter.next();
			if ( revision.getVersion().equals( version ) )
			{
				Page oldpage = getPageManager().getPage( revision.getActualPath() );
				inContext.putPageValue("oldPage", oldpage);
				if ( !session.getEditPage().isBinary() )
				{
					session.setRevisionContent( oldpage.getContent() ); //This is here in case of a merge
				}
				session.setOldPage(oldpage);
				session.setSelectedRevision(revision);
				return revision;
			}
		}
		return null;
	}
	
	public void restoreRevision(WebPageRequest inReq) throws Exception
	{
		ContentItem revision = getRevisionContent(inReq);
		if( revision != null)
		{
			RevisionSession session = getRevisions(inReq);
			saveRevision(inReq, session, revision);
			getRevisions(inReq);
		}
	}

	/**
	 * This command writes the content in the session attribute
	 * <code>revisionContent</code> as a new revision of the page given by the
	 * <code>path</code> request parameter.
	 * 
	 * @param inContext  The web page context
	 */
	public void writeRevisionContent( WebPageRequest inContext )
		throws OpenEditException
	{
		/*	TODO:Add this code	Filter filter = page.getEditFilter(); 
		boolean value= ((filter == null) || filter.passes( inContext.copy(page) ));
		
		if ( !value)
		{
			throw new OpenEditException("No permissions available");
		}
		 */		
		if ( inContext.getUser() == null)
		{
			throw new OpenEditException("No permissions available");			
		}
		RevisionSession session = getRevisions(inContext);
		ContentItem revision = session.getSelectedRevision();
		
		saveRevision(inContext, session, revision);
		//inContext.redirect(inContext.getPath() + "#reload?reload=true");
	}

	protected void saveRevision(WebPageRequest inContext, RevisionSession session, ContentItem revision)
	{
		String message = "Version "	+ revision.getVersion() + " restored.";
		String content = session.getRevisionContent();
		if ( content != null )
		{
			inContext.setRequestParameter( "message", message );
			inContext.setRequestParameter( "content", content );
			inContext.setRequestParameter( "editPath", session.getEditPath() );
			writeContent( inContext );
		}
		else
		{
			Page old = getPageManager().getPage(revision.getActualPath());
			Page current = session.getEditPage();
			current.getContentItem().setMessage(message);
			current.getContentItem().setAuthor(inContext.getUser().getUserName() );
			getPageManager().copyPage(old, current);
		}
		log.info("restored revision " + session.getEditPath() );
		inContext.removeSessionValue("revisions");
	}
	
	public void deleteAll(WebPageRequest inReq) throws OpenEditException
	{
		String path = inReq.getRequestParameter( "editPath" );
		if ( path != null)
		{
			getPageManager().getRepository().deleteOldVersions( path );
		}		
	}
}