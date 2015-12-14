/*
 * Created on Jan 7, 2004
 *
 */
package org.openedit.modules.html;


/**
 * @author Matthew Avery
 *
 */
public class Tidy
{
	//protected org.w3c.tidy.Tidy fieldJtidy;

	protected String escapeSpecialCharacters( String inRawSource )
	{
		final char NBSP = 160;

		//	replace invalid characters with their ASCII codes
		//	(e.g., the character with ASCII code 160 becomes "&#160;")
		StringBuffer escapedSource = new StringBuffer();
		for ( int n = 0; n < inRawSource.length(); n++ )
		{
			char c = inRawSource.charAt( n );
			if ( c == NBSP )
			{
				escapedSource.append( "&nbsp;" );
			}
			else if ( !Character.isISOControl( c ) || Character.isWhitespace( c ) )
			{
				escapedSource.append( c );
			}
			else
			{
				escapedSource.append( "&#" + Integer.toString( (int)c ) + ";" );
			}
		}
		return escapedSource.toString().trim();
	}
	public String removeHtml(String inHtml)
	{
		//getJtidy().parseDOM()
		//String val = inHtml.replaceAll("<br>","\n"); 
		//val = val.replaceAll("<br />","\n"); 
		String val = inHtml;
		val = val.replaceAll("<a","::link::");
		val = val.replaceAll("</a>","::closelink::"); 
		
		val = val.replaceAll("<[^>]*>","");

		val = val.replaceAll("::link::","<a");
		val = val.replaceAll("::closelink::","</a>"); 

		val = val.replaceAll("&nbsp;"," ");
		val = val.replaceAll("&quot;","\"");
		val = val.replaceAll("&trade;","TM");
		return val;
	}
	
/*	
	public String tidySource( String inRawSource, boolean inPreserveHeader )
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream( inRawSource.getBytes() );
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		getJtidy().parse( inputStream, outputStream );
		String html = outputStream.toString();;

		if ( inPreserveHeader )
		{
			return html;
		}

		if ( inRawSource.startsWith("<style"))
		{
			return inRawSource;
		}
		if ( inRawSource.startsWith("<script"))
		{
			return inRawSource;
		}
		
		String body = "<body>";
		int startIndex = html.indexOf( body );
		if ( startIndex < 0 )
		{
			return inRawSource; //We cannot handle it
		}
		else
		{
			startIndex += body.length();
		}
		String bodyclose = "</body>";
		int endIndex = html.indexOf( bodyclose );
		if ( endIndex < 0 )
		{
			endIndex = Math.max( html.length() - 1, startIndex );
		}

		String substring = html.substring( startIndex, endIndex );
		
		//TODO: we need to get back any Javascript or style stuff that got moved to the head tag
		
		
		if ( substring == null || substring.length() == 0)
		{
			return inRawSource; //so it does not delete it all
		}
		
		return escapeSpecialCharacters( substring );
	}
	protected org.w3c.tidy.Tidy getJtidy()
	{
		if ( fieldJtidy == null )
		{
			fieldJtidy = new org.w3c.tidy.Tidy();
			fieldJtidy.setWraplen(200); //we should not need to wrap stuff
			fieldJtidy.setSpaces(4);
			//fieldJtidy.setPrintBodyOnly(true);
			//fieldJtidy.setMakeClean(false);
			fieldJtidy.setXHTML(true);
			fieldJtidy.setTabsize(3);	
			fieldJtidy.setShowWarnings(false);			
			fieldJtidy.setQuiet( true );
		}
		return fieldJtidy;
	}
*/
}
