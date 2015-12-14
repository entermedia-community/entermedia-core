package org.openedit.modules.translations;

import java.util.regex.Pattern;

public class TranslationParser
{
	public static final Pattern SPLIT_REGEX = Pattern.compile("\\]\\[");
	public static final Pattern CLOSE_REGEX = Pattern.compile("\\]\\]");
	public static final Pattern OPEN_REGEX = Pattern.compile("\\[\\[");
	public static final String CLOSE = "]]";
	public static final String SPLIT = "][";
	
	protected TranslationEventListener fieldListener;
	
	public void parse( String inContent )
	{
		String[] strings = OPEN_REGEX.split(inContent );
		for ( int i = 0; i < strings.length; i++ )
		{
			String token = strings[ i ];
			
			if ( token.contains( CLOSE ) )
			{
				String[] subTokens = CLOSE_REGEX.split(token);
				String keysAndValues = subTokens[ 0 ];
				String key = keysAndValues;
				String value = keysAndValues;
				if ( keysAndValues.contains( SPLIT ) )
				{
					String[] values = SPLIT_REGEX.split(keysAndValues);//.split( SPLIT_REGEX );
					key = values[ 0 ];
					value = values[ 1 ];
				}
				fireTranslationEvent( key, value );
				if ( subTokens.length > 0 )
				{
					for ( int j = 1; j < subTokens.length; j++ )
					{
						fireTokenEvent( subTokens[ j ] );
					}
				}
			}
			else
			{
				fireTokenEvent( token );
			}
		}
	}

	protected void fireTranslationEvent( String key, String value )
	{
		if ( getListener() != null )
		{
			getListener().translationEvent( key, value );
		}
		
	}

	protected void fireTokenEvent( String token )
	{
		if ( getListener() != null )
		{
			getListener().tokenEvent( token );
		}
		
	}

	public TranslationEventListener getListener()
	{
		return fieldListener;
	}

	public void setListener( TranslationEventListener listener )
	{
		fieldListener = listener;
	}
	
	
}
