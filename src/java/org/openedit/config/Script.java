package org.openedit.config;

import java.util.HashMap;
import java.util.Map;

public class Script 
{
	protected String fieldSrc;
	protected String fieldId;
	protected boolean fieldExternal;
	protected boolean fieldDefer;
	protected Map <String, String>fieldExtraProperties;
	
	public Map <String, String> getExtraProperties()
	{
		if (fieldExtraProperties == null)
		{
			fieldExtraProperties = new HashMap();
			
		}

		return fieldExtraProperties;
	}

	public void setProperty(String inKey, String inValue) {
		getExtraProperties().put(inKey, inValue);
	}
	
	public String get(String inKey) {
		return getExtraProperties().get(inKey);
	}
	
	public boolean isDefer()
	{
		return fieldDefer;
	}
	public void setDefer(boolean inDefer)
	{
		fieldDefer = inDefer;
	}
	protected String fieldPath;
	protected boolean fieldCancel;
	
	public boolean isCancel()
	{
		return fieldCancel;
	}
	public void setCancel(boolean inCancel)
	{
		fieldCancel = inCancel;
	}
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
