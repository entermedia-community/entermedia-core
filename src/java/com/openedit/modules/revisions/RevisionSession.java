/*
 * Created on Jun 6, 2006
 */
package com.openedit.modules.revisions;

import java.util.List;

import org.openedit.repository.ContentItem;

import com.openedit.modules.edit.EditSession;
import com.openedit.page.Page;

public class RevisionSession extends EditSession
{
	protected List fieldRevisions;
	protected ContentItem fieldSelectedRevision;
	protected Page fieldOldPage;
	
	protected String fieldRevisionContent;
	
	public List getRevisions()
	{
		return fieldRevisions;
	}
	public void setRevisions(List inRevisions)
	{
		fieldRevisions = inRevisions;
	}
	public ContentItem getSelectedRevision()
	{
		return fieldSelectedRevision;
	}
	public void setSelectedRevision(ContentItem inSelectedRevision)
	{
		fieldSelectedRevision = inSelectedRevision;
	}
	public String getRevisionContent()
	{
		return fieldRevisionContent;
	}
	public void setRevisionContent(String inRevisionContent)
	{
		fieldRevisionContent = inRevisionContent;
	}
	public Page getOldPage()
	{
		return fieldOldPage;
	}
	public void setOldPage(Page inOldPage)
	{
		fieldOldPage = inOldPage;
	}
	
}
