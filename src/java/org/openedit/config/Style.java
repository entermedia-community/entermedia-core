package org.openedit.config;

public class Style
{
	protected String fieldHref;
	protected String fieldId;
	protected boolean fieldCancel;
	protected boolean fieldPreload;
	
	public boolean isPreload()
	{
		return fieldPreload;
	}
	public void setPreload(boolean inPreload)
	{
		fieldPreload = inPreload;
	}
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
	public boolean isCancel() {
		return fieldCancel;
	}
	public void setCancel(boolean inCancel)
	{
		fieldCancel = inCancel;
	}
}
