package org.openedit.profile;

import org.openedit.Data;
import org.openedit.MultiValued;

public class ModuleData
{
	protected String fieldModuleId;
	
	public ModuleData()
	{
	}
	public ModuleData(String inId, MultiValued inData)
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
	public MultiValued getData()
	{
		return fieldData;
	}
	public void setData(MultiValued inData)
	{
		fieldData = inData;
	}
	protected MultiValued fieldData;
}
