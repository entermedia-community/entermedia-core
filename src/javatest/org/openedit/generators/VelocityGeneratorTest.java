/*
 * Created on Nov 20, 2004
 */
package org.openedit.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.BaseTestCase;
import org.openedit.WebPageRequest;
import org.openedit.page.Page;
import org.openedit.page.PageSettings;
import org.openedit.repository.filesystem.StringItem;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class VelocityGeneratorTest extends BaseTestCase
{
	private static final Log log = LogFactory.getLog(VelocityGeneratorTest.class);
	public VelocityGeneratorTest( String name )
	{
		super( name );
	}

	public void testGenerate() throws Exception
	{
		WebPageRequest context = getFixture().createPageRequest("/generators/velocity.html");
		context.putPageValue("variable", ", it worked.");
		context.getPageStreamer().render();
		String result = context.getWriter().toString();
		log.info( result );
		result = result.replace("\r", "");
		assertEquals( result,"<p>\nTest stuff , it worked.\n</p>" );
	}
	
	//Does not pass because we have no way to save the encoding flag
	public void testEncoding() throws Exception
	{
		Page page = getPage("/apos.html");
		getFixture().getPageManager().removePage(page);
		
		//change the encoding
		PageSettings settings = page.getPageSettings();		
		settings.setProperty("encoding","ISO-8859-1"); //TODO: This is not ending up anyplace
		//getFixture().getPageManager().getPageSettingsManager().saveSetting(settings);
		
		//create the content
		String desc = "<DIV>Hobby Horse\u0092</DIV>";
		StringItem newItem = new StringItem(page.getPath(),desc, settings.getPageCharacterEncoding());
		newItem.setMessage("Testing");
		page.setContentItem(newItem);
		
		getFixture().getPageManager().putPage(page);		
		getFixture().getPageManager().clearCache();
		Thread.sleep(100);
		//make sure we can read it back in again
		Page reloadpage = getPage("/apos.html");		
		assertEquals( "ISO-8859-1",reloadpage.getCharacterEncoding());

		
		String reloadcontent = reloadpage.getContent();
		assertEquals(desc,reloadcontent);
		
		//TODO: Now read it back in with the wrong encoding just to be sure 
		//you get the wrong answer
		String wrongencoding = "<DIV>Hobby Horse\u2019</DIV>";
				
		//If the encoding was UTF-8 then we would have got back \u2019 instead of 92
		//92 is the correct value we expected when using this encoding

	}
}
