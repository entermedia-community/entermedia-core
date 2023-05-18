package org.openedit.profile;

import org.openedit.Data;

public class ModuleData
{
	protected String fieldModuleId;
	
	public ModuleData()
	{
	}
	public ModuleData(String inId, Data inData)
	{
		setModuleId(inId);
		setData(inData);
	}
	public String getModuleId()
	{
		return fieldModuleId;
	}
	public void setModuleId(String inModuleId)
	{
		fieldModuleId = inModuleId;
	}
	public Data getData()
	{
		return fieldData;
	}
	public void setData(Data inData)
	{
		fieldData = inData;
	}
	protected Data fieldData;
}
