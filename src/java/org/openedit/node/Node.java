package org.openedit.node;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.openedit.data.BaseData;
import org.openedit.util.Replacer;

public class Node extends BaseData
{
	public Node()
	{

	}
	
	
	public String getSetting(String inId)
	{
		return get(inId);
	}
	
	
}
