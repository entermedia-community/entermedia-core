package org.openedit.generators;

import org.openedit.Generator;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.page.Page;

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
