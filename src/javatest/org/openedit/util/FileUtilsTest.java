package org.openedit.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.openedit.BaseTestCase;
import org.openedit.util.FileUtils;

public class FileUtilsTest extends BaseTestCase{
	
	public void testIsLegalPath()
	{
		FileUtils util = new FileUtils();
		ArrayList ar = new ArrayList();
		ar.add("previous_photos/previous_photos/Photos/Institutional Advancement/DONORS/SBC/FRANK WEATHERS 10-03 COON /Weathers 01.JPG.xconf");
		ar.add("ABC\\DEFGHIJ\\KL?MNOP.xconf");
		ar.add("ABC\\DEFGHIJ\nKL\\MNOP.xconf");
		for (Iterator iterator = ar.iterator(); iterator.hasNext();) 
		{
			String path = (String) iterator.next();
			assertFalse(util.isLegalFilename(path));			
		}
	}

}
