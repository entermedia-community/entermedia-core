package com.openedit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private XmlBeanDefinitionReader reader = null;

	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}

	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.getReader().loadBeanDefinitions(resource);
	}
	public XmlBeanDefinitionReader getReader() 
	{
		if( this.reader == null)
		{
			this.reader = new XmlBeanDefinitionReader(this);
			this.reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
		}
		return reader;
	}
}
