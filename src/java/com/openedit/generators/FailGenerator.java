package com.openedit.generators;

import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;

public class FailGenerator implements Generator
{
	protected String fieldName;
	
	public FailGenerator(String inName)
	{
		fieldName = inName;
	}
	public boolean canGenerate(WebPageRequest inReq)
	{
		return false;
	}

	public void generate(WebPageRequest inContext, Page inPage, Output inOut) throws OpenEditException
	{
		throw new OpenEditException("Generator does not exist." + getName());
	}

	public String getName()
	{
		return fieldName + " (missing)";
	}

	public boolean hasGenerator(Generator inChild)
	{
		return false;
	}

	public void setName(String inName)
	{
	}
}
