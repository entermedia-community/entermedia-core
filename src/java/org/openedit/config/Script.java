package org.openedit.config;

public class Script
{
	protected String fieldSrc;
	protected String fieldId;
	protected boolean fieldExternal;
	
	public boolean isExternal() {
		return fieldExternal;
	}
	public void setExternal(boolean fieldExternal) {
		this.fieldExternal = fieldExternal;
	}
	public String getSrc()
	{
		return fieldSrc;
	}
	public void setSrc(String inSrc)
	{
		fieldSrc = inSrc;
	}
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
}
