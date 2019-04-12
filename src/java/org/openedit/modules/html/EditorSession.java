/*
 * Created on Nov 27, 2003
 *
/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/
package org.openedit.modules.html;

import org.openedit.modules.edit.EditSession;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.util.PathUtilities;
import org.openedit.util.URLUtilities;

/**
 * @author Matt Avery, mavery@einnovation.com
 */
public class EditorSession extends EditSession
{
	public static final String BODYSTART = "<BODY>";
	public static final String BODYSTART_ALTERNATE = "<body>";
	public static final String BODYEND = "</BODY>";
	public static final String BODYEND_ALTERNATE = "</body>";
//	protected HtmlWysiwygConverter fieldWysiwygConverter;
//	protected HtmlSourceConverter fieldSourceConverter;
	protected String fieldBasePath;
	protected String fieldCssPath;
	//protected String fieldHighlightCss;
	protected String fieldOriginalSource;
	protected String fieldWorkingSource;
	protected String fieldDefaultCopy = "<p>Your copy here.</p>";
	protected boolean fieldDocumentModified;
	protected boolean fieldUseDraft;
	

	public boolean isUseDraft()
	{
		return fieldUseDraft;
	}

	public void setUseDraft(boolean inUseDraft)
	{
		fieldUseDraft = inUseDraft;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	
	protected String loadContent()
	{
		String content = null;
		
		if ( isUseDraft() )
		{
			String editPath = PathUtilities.createDraftPath(getEditPath());
			Page draft = getPageManager().getPage(editPath);			
			if( draft.exists() )
			{
				//then use this content.
				content = draft.getContent();
			}
			else
			{
				if( getEditPage().exists() )
				{
					content = getEditPage().getContent();
				}
			}
		}
		else if( getEditPage().exists() )
		{
			content = getEditPage().getContent();
		}
		if ( content == null)
		{
			content = getDefaultCopy();
		}
		setOriginalSource(content);
		setWorkingSource(content);
		return content;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	protected PageManager fieldPageManager;
	
	public String createVariable(String inCode)
	{
		final int MAX_LINE_LENGTH = 300;
		StringBuffer sb = new StringBuffer();
		int linecount = 0;

		char lastC = 0;
		for ( int n = 0; n < inCode.length(); n++ )
		{
			char c = inCode.charAt( n );
			linecount++;
			if ( linecount > MAX_LINE_LENGTH)
			{
				sb.append( "\" +\n\t\"" );
				linecount = 0;
			}
			
			switch ( c )
			{
				case '\r': //This may not be needed
					if ( linecount < MAX_LINE_LENGTH/2) //only if it's a short line
					{
						sb.append("\\n");
					}
					else
					{
						sb.append( "\\n\" +\n\t\"" );
						linecount = 0;
					}
					break;
				case '\n':
				{
					if( lastC == '\r')
					{
						break;
					}
					if ( linecount < MAX_LINE_LENGTH/2) //only if it's a short line
					{
						sb.append("\\n");
					}
					else
					{
						sb.append( "\\n\" +\n\t\"" );
						linecount = 0;
					}
					break;
				}
				case '\"':
					sb.append( "\\\"" );
					break;
				case '/':
					//	"//" is interpreted as start of comment even if within string (JavaScript interpreter bug)
					//	Therefore, "//" must be split across two lines
					if ( lastC == '/' )
					{	
						sb.append( "\" +\n\t\"" );
						linecount = 0;
					}
					sb.append( '/' );
				break;
			case 't':
			case 'T':
				//	Don't allow the word "script" to appear without splitting it across lines, because of another
				//	bug in the JavaScript interpreter.
				if ( sb.length() > 5 && sb.substring(sb.length() - 5).equalsIgnoreCase("SCRIP") )
				{
					sb.append( "\" +\n\t\"" );
					linecount = 0;
				}
				sb.append( c );
				break;
			case '\\':
			    sb.append( "\\\\");
			    break;
			default:
				sb.append( c );
			}
			lastC = c;
		}
		return sb.toString();
	}

/*	public HtmlWysiwygConverter getWysiwygConverter( WebPageRequest inContext )
	{
		if (fieldWysiwygConverter == null)
		{
			String userAgent = inContext.getRequest().getHeader( "User-Agent" );
			if ( userAgent.indexOf("Gecko") > -1 )
			{
				fieldWysiwygConverter = new MozillaHtmlWysiwygConverter();
			}
			else
			{
				if ( userAgent.indexOf( "MSIE 6.0") > -1 )
				{
					fieldWysiwygConverter = new IE60HtmlWysiwygConverter();
				}
				else
				{
					fieldWysiwygConverter = new IE55HtmlWysiwygConverter();
				}
			}
		}
		return fieldWysiwygConverter;
	}
*/
/*	public String escapeSource(String inContent, WebPageRequest inContext) throws Exception
	{
		String sourceContent = getSourceConverter().toDisplayCode(inContent);
		String finalHtml = "<html><head><base href='" + getBasePath() + "'>";
		finalHtml += "<style type='text/css'>";
		finalHtml += getExternalCss();
		finalHtml += "</style>";
		finalHtml += "</head>";
		finalHtml += BODYSTART;
		finalHtml += URLUtilities.xmlEscape( sourceContent);
		finalHtml += BODYEND;
		finalHtml += "</html>";

		return finalHtml;
	}
*/
//	public String wrapForWysiwyg(String inHtml)
//	{
//		if (hasHeader())
//		{
//			// Need to insert a "base" tag for images to work.
//			inHtml = inHtml.replaceFirst("<HEAD>", "<HEAD><base href=\"" + getBasePath() + "\">");
//			//alert( inHtml );
//			return inHtml;
//		}
//		else
//		{
//			//using a link tag breaks gecko
//			String html = "<html><head><base href='" + getBasePath() + "'>\n";
////			html += "<style type='text/css'>\n";
////			html += getExternalCss();
////			html += "</style>";
//
//			html += "</head>";
//			html += BODYSTART;
//			html += inHtml;
//			html += BODYEND;
//			html += "</html>";
//			return html;
//		}
//	}

	
	
	public String getWysiwygSource()
	{
		//return wrapForWysiwyg( getWorkingSource() );
		return getWorkingSource();
	}
	
	public String getWysiwygSourceVariable()
	{
		return createVariable( getWysiwygSource() );
	}
	
	public boolean hasHeader()
	{
		String lower = getOriginalSource().toLowerCase();
		if (lower.indexOf("<html") > -1 )
		{
			if( lower.indexOf("<body") > -1 )
			{
				return true;
			}
		}
		return false;
	}

	public String getBasePath()
	{
		return fieldBasePath;
	}

//	public String getExternalCss()
//	{
//		StringBuffer out = new StringBuffer();
//		if ( getFontsCss() != null)
//		{
//			out.append( getFontsCss() );
//		}
//		if ( getHighlightCss() != null)
//		{
//			out.append( "\n");
//			out.append( getHighlightCss() );
//		}
//		return out.toString();
//	}

	public void setBasePath(String string)
	{
		fieldBasePath = string;
	}

	public String getOriginalSource()
	{
		if ( fieldOriginalSource == null )
		{
			loadContent();
		}
		return fieldOriginalSource;
	}

	public void setOriginalSource(String string)
	{
		fieldOriginalSource = string;
	}
	
/*	public HtmlSourceConverter getSourceConverter()
	{
		if (fieldSourceConverter == null)
		{
			fieldSourceConverter = new HtmlSourceConverter();
		}
		return fieldSourceConverter;
	}
*/
	public String getWorkingSource()
	{
		if ( fieldWorkingSource == null )
		{
			loadContent();
		}
		return fieldWorkingSource;
	}
	
	public String getEscapedSource()
	{
		String html = URLUtilities.xmlEscape(getWorkingSource());
		//html = SpecialCharacter.escapeSpecialCharacters( html );
		return html;
	}

	public void setWorkingSource(String inWorkingSource)
	{
		fieldWorkingSource = inWorkingSource;
	}

	/**
	 * @param inContent
	 * @param inInContext
	 * @return
	 */
	/**
	 * @param inContent
	 * @return
	 */

	public boolean isFullPage()
	{
		String src = getWorkingSource();
		if( src != null)
		{
			if ( src.toLowerCase().contains("<body") )
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isDocumentModified() {
		return fieldDocumentModified;
	}

	public void setDocumentModified(boolean inB) {
		fieldDocumentModified = inB;
	}

	public String getDefaultCopy()
	{
		return fieldDefaultCopy;
	}
	public void setDefaultCopy( String defaultCopy )
	{
		fieldDefaultCopy = defaultCopy;
	}
	public String removeBaseHrefAndFixQuotes(String inContent)
    {
        //(?s)fun*
        //String content = inContent.replaceAll("(?s)_base_href=\".*\" ","");
        if ( inContent == null)
        {
            return null;
        }
        String content = inContent.replaceAll("_base_href=\"([^\"]+)\"","");

        content = content.replaceAll("&quot;","\"");

        //removed since most times &amp; is what we want to have 
        //content = content.replaceAll("&amp;","&");
       
        //Comments are loosing the \n at the end in IE look for space
        content = content.replaceAll("--> ", "-->\n");
        content = content.replace("spellcheck=\"true\"", ""); //FCK editor is leaving this in
        
        return content;
    }

	public String getCssPath()
	{
		return fieldCssPath;
	}

	public void setCssPath(String inCssPath)
	{
		fieldCssPath = inCssPath;
	}
	public String stripBody(String inContent)
	{
		String content = inContent;
		int bodyStartIndex = content.indexOf( EditorSession.BODYSTART );
		if ( bodyStartIndex < 0 )
		{
			bodyStartIndex = content.indexOf( EditorSession.BODYSTART_ALTERNATE );
		}
		if ( bodyStartIndex >= 0 )
		{
			content = content.substring( bodyStartIndex + EditorSession.BODYSTART.length() );
		}
		int bodyEndIndex = content.lastIndexOf( EditorSession.BODYEND );
		if ( bodyEndIndex < 0 )
		{
			bodyEndIndex = content.indexOf( EditorSession.BODYEND_ALTERNATE );
		}
		if ( bodyEndIndex >= 0 )
		{
			content = content.substring( 0, bodyEndIndex );
		}
		
		return content;
	}
}
