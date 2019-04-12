/*
 * Created on Aug 30, 2004
 */
package org.openedit.util;

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
        String good = "https://user@somesite.com/this has space/You're & crazy/index.html?somejunk=bad%2C+%2B+%25+%40+%21++%5C+%2F%2F%3B+stuff";
        assertEquals(good, fixed);
    
    }

    public void testUrlPath()
    {
    	String url = "/this [has] space/You're & crazy/index.html?this=should&workd=work";
    	
        String fixed = URLUtilities.urlEscape(url);
       // String good = "/this%20has%20space/You%27re%20%26%20crazy/index.html?this=should&workd=work";
        String good = "/this %5Bhas%5D space/You're & crazy/index.html?this=should&workd=work";
        assertEquals(good, fixed);
    
    }

    public void testUrlParams()
    {
    	String url = "https://user@somesite.com/this [has] space/You're & crazy/index.html?this=should&workd=work";
    	
        String fixed = URLUtilities.urlEscape(url);
        String good = "https://user@somesite.com/this %5Bhas%5D space/You're & crazy/index.html?this=should&workd=work";
        assertEquals(good, fixed);
    
    }

    
    
}
