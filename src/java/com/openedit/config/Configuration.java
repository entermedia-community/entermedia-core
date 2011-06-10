/*
 * Created on Oct 26, 2004
 */
package com.openedit.config;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author cburkey
 *
 */
public  interface Configuration extends Serializable
{

	/**
	 * @param inString
	 * @return
	 */
	public List getChildren(String inString);
	
	public Configuration getParent();
	/**
	 * @param inString
	 * @return
	 */
	public String getAttribute(String inString);
	
	public String get(String inString);

	public void setAttribute(String inKey, String inValue);
	
	Configuration addChild(String inChild);
	Configuration addChild(Configuration inChild);

	public String getValue();
//	
	void setValue(String inValue);
	/**
	 * @param inString
	 * @return
	 */
	public Configuration getChild(String inString);

	/**
	 * @return
	 */
	public String getName();
	public void setName(String inName);

	/**
	 * @return
	 */
	public List getChildren();
	/**
	 * @param inString
	 * @return
	 */
	public String getChildValue(String inString);
	/**
	 * @param inArg0
	 * @return
	 */
	public List getAttributeNames();

	public void removeChild(Configuration inConfig);
	
	public Iterator getChildIterator(String inName);

	/**
	 * @param inConfiguration
	 */
	public void setParent(Configuration inConfiguration);

	public boolean hasChildren();
}
