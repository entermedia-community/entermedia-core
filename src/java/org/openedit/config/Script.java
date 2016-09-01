package org.openedit.config;

public class Script
{
	protected String fieldSrc;
	protected String fieldId;
	protected boolean fieldExternal;
	protected String fieldPath;
	
	public String getPath()
	{
		return fieldPath;
	}
	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}
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
