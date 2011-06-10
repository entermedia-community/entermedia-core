/*
 * Created on Nov 28, 2003
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
package com.openedit.modules.html;

/**
 * @author Matt Avery, mavery@einnovation.com
 */
public class Token
{
	protected boolean fieldInQuotes;
	protected boolean fieldInTag;
	protected boolean fieldInComment;
	protected boolean fieldInParagraph;
	
	protected String fieldText;
	
	public Token( String inTokenString )
	{
		fieldText = inTokenString;
	}

	public boolean isAttribute()
	{
		if ( isInQuotes() || isInComment() )
		{
			return false;
		}
		if ( isInTag() )
		{
			if ( getText().indexOf( '<' ) >= 0 
			  || getText().indexOf( '>' ) >= 0 )
			{
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean isEntity()
	{
		return getText().indexOf( '&' ) == 0 
			&& getText().indexOf( ';' ) == ( getText().length() - 1 );
	}

	public boolean isInComment()
	{
		return fieldInComment;
	}

	public boolean isInQuotes()
	{
		return fieldInQuotes;
	}

	public boolean isInTag()
	{
		return fieldInTag;
	}

	public String getText()
	{
		return fieldText;
	}

	public void setInComment(boolean b)
	{
		fieldInComment = b;
	}

	public void setInQuotes(boolean b)
	{
		fieldInQuotes = b;
	}

	public void setInTag(boolean b)
	{
		fieldInTag = b;
	}

	public void setText(String string)
	{
		fieldText = string;
	}

	public boolean isInParagraph()
	{
		return fieldInParagraph;
	}

	public void setInParagraph(boolean b)
	{
		fieldInParagraph = b;
	}

}
