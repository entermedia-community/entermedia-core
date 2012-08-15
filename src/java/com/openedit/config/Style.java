package com.openedit.config;

public class Style
{
	protected String fieldHref;
	protected String fieldId;
	public String getHref()
	{
		return fieldHref;
	}
	public void setHref(String inSrc)
	{
		fieldHref = inSrc;
	}
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	protected Boolean fieldExternal;
	
	public Boolean getExternal() {
		return fieldExternal;
	}
	public void setExternal(Boolean fieldExternal) {
		this.fieldExternal = fieldExternal;
	}
}
