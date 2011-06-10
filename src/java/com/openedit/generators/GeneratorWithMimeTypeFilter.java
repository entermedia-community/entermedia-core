/*
 * Created on Jun 5, 2005
 */
package com.openedit.generators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;

/**
 * @author cburkey
 *
 */
public class GeneratorWithMimeTypeFilter extends BaseGenerator
{

	protected Generator fieldGenerator;
	protected List fieldMimeTypes;
	public GeneratorWithMimeTypeFilter(Generator inGen, String inTypes)
	{
		setGenerator(inGen);
		List types = Arrays.asList(inTypes.split(","));
		setMimeTypes(types);
	}
	
	public void generate(WebPageRequest inContext, Page inPage, Output inOut) throws OpenEditException
	{
		getGenerator().generate(inContext,inPage, inOut);
	}

	public Generator getGenerator()
	{
		return fieldGenerator;
	}
	public void setGenerator(Generator inGenerator)
	{
		fieldGenerator = inGenerator;
	}
	public List getMimeTypes()
	{
		return fieldMimeTypes;
	}
	public void setMimeTypes(List inMimeTypes)
	{
		fieldMimeTypes = inMimeTypes;
	}
	
	public boolean canGenerate(WebPageRequest inReq)
	{
		if( inReq == null)
		{
			return false;
		}
		String compareTo = inReq.getPage().getMimeType();
		for (Iterator iter = getMimeTypes().iterator(); iter.hasNext();)
		{
			String mtype = (String) iter.next();
			if ( mtype.equalsIgnoreCase(compareTo))
			{
				return true;
			}
			
		}
		return false;
	}

}
