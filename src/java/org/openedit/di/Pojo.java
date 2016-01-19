package org.openedit.di;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class Pojo
{	
	public static final Namespace lang = new Namespace("lang", "http://www.springframework.org/schema/lang");
	public static final QName groovy = new QName("groovy", lang);
	public static final QName gproperty = new QName("property", lang);

	
	Element fieldConfig;
	Object fieldSingleton;
	
	public Object getSingleton()
	{
		return fieldSingleton;
	}
	public void setSingleton(Object inSingleton)
	{
		fieldSingleton = inSingleton;
	}
	public boolean isSingleton()
	{
		String scope = fieldConfig.attributeValue("scope");
		if( scope == null || !scope.equals("prototype"))
		{
			return true;
		}
		return false;
	}
	public String getClassPath()
	{
		String scope = fieldConfig.attributeValue("class");
		if( scope== null)
		{
			scope = fieldConfig.attributeValue("script-source");
		}
		return scope;
	}
	public List getProperties()
	{
		List elements = fieldConfig.elements("property");
		if( elements == null)
		{
			elements =   fieldConfig.elements(gproperty);
		}
		return elements;
	}

}
