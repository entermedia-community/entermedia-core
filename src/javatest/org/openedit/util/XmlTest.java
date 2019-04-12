package org.openedit.util;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.openedit.BaseTestCase;
import org.openedit.page.Page;
import org.openedit.users.User;
import org.openedit.util.XmlUtil;

public class XmlTest extends BaseTestCase
{
	private static final Log log = LogFactory.getLog(XmlTest.class);
	public XmlTest()
	{
		// TODO Auto-generated constructor stub
	}
	public void testXmlReader() throws Exception
	{
		Element root = DocumentHelper.createDocument().addElement("mounts");
		for (int i = 0; i < 10000; i++)
		{
			Element e =root.addElement("mount");
			e.addAttribute("id", "1234" + i);
			e.addAttribute("path", "/home/stuff/" + i);
			
			e.addElement("child1").setText("This is a long text" + i);
			e.addElement("child2").setText("This is a long text" + i);
			e.addElement("child3").setText("This is a long text" + i);
			//e.addElement("childContent").addElement("childA").addElement("childB").addElement("childC").addElement("childD").addAttribute("Dattrib", "sdfsddsfdsfsdf");
		}

		XmlUtil xml = new XmlUtil();

		User user = getFixture().createPageRequest().getUser();
		Page junk = getPage("/WEB-INF/junksave.xml");

		long start = System.currentTimeMillis();
//		for (int i = 0; i < 100; i++)
//		{
//			StringWriter out = new StringWriter();
//			xml.saveXml(root, out, "UTF-8");
//			getFixture().getPageManager().saveContent( junk,user,out.toString(),null);
//		}
//		log.info("Saved String in " + (System.currentTimeMillis() - start)/1000F + " seconds");

		
		start = System.currentTimeMillis();
		
		for (int i = 0; i < 100; i++)
		{
//			StringWriter out = new StringWriter();
//			new OutputFiller().fill(new ElementReader(root,xml.getWriter("UTF-8")),out);
//			log.info(out.toString());
//			xml.saveXml(root, out, "UTF-8");
//			getFixture().getPageManager().saveContent( junk,user,new StringReader(out.toString()),null);
			

//			getFixture().getPageManager().saveContent( junk,user,new ElementReader(root,xml.getWriter("UTF-8")),null);

			
			OutputStream out = getFixture().getPageManager().saveToStream(junk, user, null);
			XMLWriter writer = xml.getWriter("UTF-8");
			writer.setOutputStream(out);
			writer.write(root);
			out.close();
		}
		log.info("Saved Reader in " + (System.currentTimeMillis() - start)/1000F + " seconds");


	}

}
