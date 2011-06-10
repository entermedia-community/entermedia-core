/*
 * Created on Dec 7, 2003
 *
/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/
package com.openedit.modules;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.openedit.TestWebPageRequest;
import com.openedit.WebPageRequest;
import com.openedit.modules.reflect.Reflector;

/**
 * @author Matt Avery, mavery@einnovation.com
 */
public class ReflectorTest extends TestCase
{
	public static final String KEY = "test-object.testMethod";
	protected Reflector fieldReflector;
	protected WebPageRequest fieldContext;

	public ReflectorTest(String arg0)
	{
		super(arg0);
	}

	//	public static void main(String[] args)
	//	{
	//		junit.swingui.TestRunner.run(ReflectorTest.class);
	//	}

	protected void setUp() throws Exception
	{
		WebPageRequest context = getWebPageContext();
		context.setRequestParameter(KEY, "Hello");
	}

	public void testFindMethod()
	{
		class TestObject
		{
			public void testMethod(WebPageRequest inContext)
			{
			}
		}
		Object sessionObject = new TestObject();
		int dotIndex = KEY.indexOf('.');
		String methodName = KEY.substring(dotIndex + 1, KEY.length());
		List methods = getReflector().findMethods(sessionObject, methodName);
		assertEquals( 1, methods.size() );
		class TestObject2
		{
			public void notTheMethod(WebPageRequest inContext)
			{
			}
		}
		sessionObject = new TestObject2();
		methods = getReflector().findMethods(sessionObject, methodName);
		assertEquals( 0, methods.size() );
		
		class TestObject3
		{
			protected boolean fieldDateStringBoolean;
			protected boolean fieldDateBoolean;
			public void setDate(String inDateString)
			{
				fieldDateStringBoolean = true;
			}
			
			public void setDate( Date inDate )
			{
				fieldDateBoolean = true;
			}
			
			public boolean dateStringSet()
			{
				return fieldDateStringBoolean;
			}
			
			public boolean dateSet()
			{
				return fieldDateBoolean;
			}
		}
		Object testObject = new TestObject3();
		methods = getReflector().findMethods(testObject, "setDate");
		assertEquals( 2, methods.size() );
	}

	public void testReflectOnRequestParameters() throws Exception
	{
		TestObject testObject = new TestObject();
		getWebPageContext().setRequestParameter("setString", "Hello");
		getWebPageContext().setRequestParameter("setSomething", "goodbye");
		getWebPageContext().setRequestParameter("setStringArray", "Hello");
		getWebPageContext().setRequestParameter("setInt", "5");
	//	getWebPageContext().setRequestParameter("setInvalidSetter", "blah");
		getReflector().reflectOnRequestParameters(getWebPageContext(), testObject);
		assertEquals("Hello", testObject.getString());
		assertEquals("Hello", testObject.getStringArray()[0]);
		assertEquals(5, testObject.getInt());
		assertTrue( !testObject.invalidSetterCalled );
		
		getWebPageContext().setRequestParameter(
			"setStringArray",
			new String[] { "Yahoo", "Goodbye" });
		getReflector().reflectOnRequestParameters(getWebPageContext(), testObject);
		assertEquals("Yahoo", testObject.getStringArray()[0]);
		assertEquals("Goodbye", testObject.getStringArray()[1]);
	}

	public Reflector getReflector()
	{
		if (fieldReflector == null)
		{
			fieldReflector = new Reflector();
		}
		return fieldReflector;
	}

	private WebPageRequest getWebPageContext()
	{
		if (fieldContext == null)
		{
			fieldContext = new TestWebPageRequest();
		}
		return fieldContext;
	}
	
}
