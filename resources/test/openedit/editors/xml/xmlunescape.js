function xmlUnescape( str )
{
	if ( str.indexOf( '&' ) == -1 )
	{
		return str;
	} 
	var result = "";
	var index = 0;
	var c = '';
	while ( index < str.length )
	{
		c = str.charAt( index );
		if ( c == '&' )
		{
			// Handle character entities.
			
			if ( str.charAt( index + 1 ) == '#' )
			{
				var radix = 10;
				var startIndex = index + 2;
				if ( str.charAt( index + 2 ) == 'x' )
				{
					radix = 16;
					startIndex++;
				}
				var semicolonIndex = str.indexOf( ';', startIndex );
				if ( semicolonIndex >= 0 )
				{
					// FIXME: Do some error checking here.
					charIndex = parseInt( str.substring( startIndex, semicolonIndex ), radix );
					result += String.fromCharCode( charIndex );
					index = semicolonIndex + 1;
					continue;
				}
			}
			else
			{
				var semicolonIndex = str.indexOf( ';', index + 1 );
				if ( semicolonIndex >= 0 )
				{
					var charToAppend = '';
					// FIXME: Do some error checking here.
					entityName = str.substring( index + 1, semicolonIndex );
					if ( entityName == "lt" )
					{
						charToAppend = "<";
					}
					else if ( entityName == "gt" )
					{
						charToAppend = ">";
					}
					else if ( entityName == "amp" )
					{
						charToAppend = "&";
					}
					else if ( entityName == "quot" )
					{
						charToAppend = '"';
					}
					else if ( entityName == "apos" )
					{
						charToAppend = "'";
					}
					
					if ( charToAppend != "" )
					{
						result += charToAppend;
						index = semicolonIndex + 1;
						continue;
					}
				}
			}
		}
		
		result += c;
		index++;
	}
	
	return result;
}
