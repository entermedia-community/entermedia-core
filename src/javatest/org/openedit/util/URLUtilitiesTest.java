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

}
