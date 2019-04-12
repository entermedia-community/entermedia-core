package org.openedit.node;

import org.openedit.data.BaseData;

public class Node extends BaseData
{
	public static final String PRIMARY = "primary";
	
	public Node()
	{

	}
	
	
	public String getSetting(String inId)
	{
		return get(inId);
	}
	
	public String getNodeType()
	{
		String nodetype = get("entermedia.nodetype");
		if( nodetype == null)
		{
			nodetype = PRIMARY;
		}
		return nodetype;
	}
}
