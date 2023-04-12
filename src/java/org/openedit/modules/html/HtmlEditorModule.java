/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.openedit.modules.html;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.modules.edit.BaseEditorModule;
import org.openedit.page.Page;
import org.openedit.page.Permission;
import org.openedit.util.PathUtilities;
import org.openedit.util.URLUtilities;

/**
 * @author dbrown
 * @author Matt Avery, mavery@einnovation.com
 */
public class HtmlEditorModule extends BaseEditorModule
{
	private static final Log log = LogFactory.getLog(HtmlEditorModule.class);
	/**
	 * Loads the WYSIWYG view
	 * @param inReq
	 * @throws Exception
	 */
	public void loadView( WebPageRequest inReq) throws Exception
	{
		EditorSession session = startEditSession(inReq);	
		if( session != null)
		{
			
			//inReq.putPageValue("viewcontent",  session.getWysiwygSourceVariable() );
	        //inReq.putPageValue("rawviewcontent", session.getWysiwygSource() );
	        inReq.putPageValue("csspath", session.getCssPath() );
	        inReq.putPageValue("editPage",session.getEditPage() );
		}
		//inReq.putPageValue( "documentModified", new Boolean( session.isDocumentModified() ) );
	}
	/**
	 * Load the source code into the source code editor
	 * @param inReq
	 * @throws Exception
	 */
	public void loadSource( WebPageRequest inReq ) throws Exception
	{
		EditorSession session = startEditSession(inReq);
/*		String type = inReq.getRequestParameter("type");
		if ( "text".equals( type ))
		{
			inReq.putPageValue( "sourcecontent",  session.getOriginalSource() );
		}
		else
		{
*/			inReq.putPageValue( "sourcecontent",  session.getEscapedSource() );
	//	}
		inReq.putPageValue( "documentModified", new Boolean( session.isDocumentModified() ) );		
	}

	/**
	 * @param inEditorSession
	 * @param inEditPath
	 */
	protected EditorSession startEditSession(WebPageRequest inReq) throws OpenEditException
	{
		String editPath = inReq.getRequestParameter("editPath");

		if( editPath == null)
		{
				editPath = inReq.getRequestParameter("path");
		}

		if( editPath == null)
		{
			return null;
		}
		EditorSession inEditorSession = new EditorSession();
		inEditorSession.setPageManager(getPageManager());
		Page editPage = getPageManager().getPage( editPath, true );
		
		boolean multipleLang = true;
		String savein = inReq.getPageProperty("usemultiplelanguages");
		if (  savein != null )
		{
			multipleLang = Boolean.parseBoolean(savein);
		}
		else
		{
			multipleLang = false;
		}

		String selectedcode = inReq.getLanguage();
		String rootdir = "/translations/" + selectedcode;
		if( multipleLang )
		{
			if(  selectedcode == null || selectedcode.equals("default") || editPath.startsWith(rootdir) )
			{
				multipleLang = false;
			}
		}
		if( multipleLang )
		{
			editPath = rootdir + editPath;
		}		
		boolean useDraft = createDraft(editPage,inReq);
		inEditorSession.setUseDraft(useDraft);
		editPage = getPageManager().getPage(editPath, true); 
		inEditorSession.setEditPage(editPage);
		String origUrl = inReq.getRequestParameter("origURL");
		inEditorSession.setOriginalUrl(origUrl);
		
		URLUtilities urlUtilities = (URLUtilities) inReq.getPageValue( "url_util" );
		if( urlUtilities != null)
		{
			inEditorSession.setBasePath( urlUtilities.buildStandard( editPath ) );
		}
		//Is this being used anymore?
		String parentName = inReq.getRequestParameter( "parentName" );
		inEditorSession.setParentName( parentName );

		String location = editPage.get("editstylesheet");
		if( location == null)
		{		
			String il = editPage.getInnerLayout();
			if( il != null )
			{
				location = PathUtilities.extractDirectoryPath(il) + "/style.css";
			}
		}
		if( location == null)
		{
			location = "/_styles.css";
		}
		inEditorSession.setCssPath(location);

		Page styles = getPageManager().getPage(location);
		if( !styles.exists())
		{
			log.debug("No CSS file used in editor: " + location);
		}
		inReq.putPageValue( "editorSession", inEditorSession );
		inReq.putPageValue("editPath",inEditorSession.getEditPath());
		
		return inEditorSession;
	}
	
	public void save(WebPageRequest inReq) throws Exception
	{
		//Strip the body junk		
		EditorSession session = startEditSession(inReq);
		String content = inReq.getRequestParameter("content");
		if( content == null)
		{
			content = "";
		}
		String type = inReq.getRequestParameter("contenttype");
		if( !"text".equals(type) )
		{
			content = session.removeBaseHrefAndFixQuotes( content );
		}
		inReq.setRequestParameter("content",content);
		session.setWorkingSource( content );
		String path = inReq.getRequestParameter("savepath");
		if ( path == null)
		{
			inReq.setRequestParameter("savepath",session.getEditPath());
		}
		inReq.setRequestParameter("editPath",session.getEditPath());
		writeContent( inReq );
		
	}
	protected String getContent( String inPath ) throws Exception
	{
		Page page = getPageManager().getPage( inPath );
		if ( page.exists() )
		{
			return page.getContent();
		}
		return "";
	}
	
	
	public void loadEditor(WebPageRequest inReq){
		String openeditid = inReq.findReqValue("openeditid");
		inReq.putPageValue("openeditid", openeditid);
		inReq.putPageValue("oehome", "/" + openeditid);
		String linkedcatalog = inReq.findReqValue("linkedcatalog");
		inReq.putPageValue("linkedcatalog", linkedcatalog);
		
	}
	
	public void loadCatalogPermissions(WebPageRequest inReq){
		
		String linkedcatalog = inReq.findValue("linkedcatalog");
		
		String path = "/"+ linkedcatalog + "/" ;
		
		List names = Arrays.asList(new String[]{"upload","download","forcewatermark","editasset", "viewasset", "view"});
		
		Page page = getPageManager().getPage(path);
		WebPageRequest req = inReq.copy(page);
		for (Iterator iterator = names.iterator(); iterator.hasNext();)
		{
			String pername = (String) iterator.next();
			Permission per = page.getPermission(pername);
			if (per != null)
			{
				boolean value = per.passes(req);
				//log.info(getCatalogId() + " " + pername + " = " + value + " " + per.getPath());
				inReq.putPageValue("can" + per.getName(), Boolean.valueOf(value) );
			}
		}
	}
	
}
