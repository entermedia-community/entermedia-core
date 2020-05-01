/*
 * Created on Aug 30, 2004
 */
package org.openedit.util;

import java.io.UnsupportedEncodingException;
import java.util.Formatter;

import org.openedit.BaseTestCase;
import org.openedit.util.URLUtilities;

/**
 * @author Eric Broyles <eric.broyles@ugs.com>
 */
public class URLUtilitiesTest extends BaseTestCase
{

    public URLUtilitiesTest( String arg0 )
	{
		super( arg0 );
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
    {
        junit.textui.TestRunner.run(URLUtilitiesTest.class);
    }
    
    public void testGetPathWithoutContext()
    {
        String path = URLUtilities.getPathWithoutContext("/resellerportal", "/include.jsp", "index.html");
        assertEquals("/include.jsp", path);
    }

    public void testFullUrlEscape()
    {
    	String url = "https://user@somesite.com/this has space/You're & crazy/index.html?somejunk=bad, + % @ !  \\ //; stuff";
    	
        String fixed = URLUtilities.urlEscape(url);
        String good = "https://user@somesite.com/this%20has%20space/You're%20&%20crazy/index.html?somejunk=bad%2C+%2B+%25+%40+%21++%5C+%2F%2F%3B+stuff";
        assertEquals(good, fixed);
    
    }

    public void testUrlPath()
    {
    	String url = "/this [has] space/You're & crazy/index.html?this=should&workd=work&nothing=";
    	
        String fixed = URLUtilities.urlEscape(url);
       // String good = "/this%20has%20space/You%27re%20%26%20crazy/index.html?this=should&workd=work";
        String good = "/this%20%5Bhas%5D%20space/You're%20&%20crazy/index.html?this=should&workd=work&nothing=";
        assertEquals(good, fixed);
    
    }
    public void testUtf8UrlPath()
    {
    	String url = "ège-flight-supply-covid.jpg";
    	
        String fixed = URLUtilities.urlEscape(url);
        String good = "e%CC%80ge-flight-supply-covid.jpg";
        assertEquals(good, fixed);
    
    }

    public void testUrlParams()
    {
    	String url = "https://user@somesite.com/this [has] space/You're & crazy/index.html?this=should&workd=work";
    	
        String fixed = URLUtilities.urlEscape(url);
        String good = "https://user@somesite.com/this%20%5Bhas%5D%20space/You're%20&%20crazy/index.html?this=should&workd=work";
        assertEquals(good, fixed);
    
    }
    
    public void testMessage()
    {
    	String url = "Hi Eye <br> <div> https://app.slack.com </div>";
    	
        String fixed = URLUtilities.escapeMessage(url);
        String good = "Hi Eye <br> &lt;div&gt; <a href=\"https://app.slack.com\">https://app.slack.com</a> &lt;/div&gt;";
        assertEquals(good, fixed);
    
    }

    
    public void testJava() 
    {
	  	// See http://www.fileformat.info/info/unicode/char/1f4a9/index.htm
		final String poo = "💩 è"; 
		
		System.out.println(poo);
		// Length of chars doesn't equals the "real" length, that is: the number of actual codepoints
		System.out.println(poo.length() + " vs " + poo.codePointCount(0, poo.length()));
		
		Formatter formatter = new Formatter();
		try
		{
			byte[] all;
				all = poo.getBytes("UTF-8");
			for (byte b : all) 
			{
	            formatter.format("%%%02X", b);
	        }
    }	
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done " + formatter);
		// Iterating over all chars
		for(int i=0; i<poo.length();++i) {
			char c = poo.charAt(i);
			// If there's a char left, we chan check if the current and the next char 
			// form a surrogate pair
			if(i<poo.length()-1 && Character.isSurrogatePair(c, poo.charAt(i+1))) {
				// if so, the codepoint must be stored on a 32bit int as char is only 16bit
				int codePoint = poo.codePointAt(i);
				// show the code point and the char
				System.out.println(String.format("%6d:%s", codePoint, new String(new int[]{codePoint}, 0, 1)));
				++i;
			}
			// else this can only be a "normal" char
			else 
				System.out.println(String.format("%6d:%s", (int)c, c));
		}
		
		// constructing a string constant with two \\u unicode escape sequences
		System.out.println("\ud83d\udca9".equals("💩"));
	}
}
    
    
