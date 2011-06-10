/*
 * Created on Nov 29, 2003
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
package com.openedit.modules.reflect;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.modules.BaseModule;

/**
 * @author Matt Avery, mavery@einnovation.com
 * 
 * Could make this an immutable object and eliminate the WebPageContext argument
 * to the public methods.
 */
public class Reflector extends BaseModule
{
	public void reflectOnSessionObjects( WebPageRequest inContext ) throws OpenEditException
	{
		
		for (Iterator iter = inContext.getParameterMap().keySet().iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			int dotIndex = key.indexOf('.');
			if (dotIndex > 0)
			{
				String sessionObjectKey = key.substring(0, dotIndex);
				Object sessionObject = inContext.getSessionValue(sessionObjectKey);
				if (sessionObject == null)
				{
					return;
				}
				String methodName = key.substring(dotIndex + 1, key.length());
				List methods = findMethods(sessionObject, methodName);
				for ( Iterator iterator = methods.iterator(); iterator.hasNext(); )
				{
					Method method = (Method) iterator.next();
					try
					{
						invokeMethod(inContext, method, sessionObject);
					}
					catch (Exception ex)
					{
						//log.error( ex );
						if ( ex instanceof OpenEditException)
						{
							throw (OpenEditException)ex;
						}
						throw new OpenEditException(ex);
					}
				}
			}
		}

	}

	/** Two methods could have the same name but different signatures
	 * 
	 * @param inObject
	 * @param inMethodName
	 * @return
	 */
	public List findMethods(Object inObject, String inMethodName)
	{
		List methods = new ArrayList();
		MethodDescriptor[] methodDescriptors;
		try
		{
			methodDescriptors = Introspector.getBeanInfo(inObject.getClass()).getMethodDescriptors();
		}
		catch (IntrospectionException e)
		{
			return methods;
		}
		for (int i = 0; i < methodDescriptors.length; i++)
		{
			if ( methodDescriptors[i].getName().equals( inMethodName ) )
			{
				methods.add( methodDescriptors[i].getMethod() );
			}
		}
		return methods;
	}

	protected void invokeMethod(WebPageRequest inContext, Method method, Object sessionObject) throws Exception
	{
		try
		{
			method.invoke(sessionObject, new Object[] { inContext });
		}
		catch (InvocationTargetException ite)
		{
			Throwable throwable = ite.getTargetException();
			if (throwable instanceof Exception)
			{
				throw (Exception) throwable;
			}
			/*
			else if (throwable instanceof Message)
			{
				throw (Message) throwable;
			}
				*/
			else if (throwable instanceof Error)
			{
				throw (Error) throwable;
			}
			else
			{
				throw new OpenEditException(throwable.getMessage());
			}
		}

	}
	public void reflectOnRequestParameters(WebPageRequest inContext, Object inObject) throws Exception
	{
		Set keys = inContext.getParameterMap().keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			Object value;
			List methods = findMethods( inObject, key );

			for ( Iterator iterator = methods.iterator(); iterator.hasNext(); )
			{
				Method method = (Method) iterator.next();
				
				Class[] parameterTypes = method.getParameterTypes();
				if ( parameterTypes.length == 1 )
				{
					Class parameterType = parameterTypes[0];
					if ( parameterType.equals( String.class ) )
					{
						value = inContext.getRequestParameter( key );
					}
					else if ( parameterType.equals( String[].class ) )
					{
						value = inContext.getRequestParameters( key );
					}
					else if ( parameterType.equals( int.class ) )
					{
						value = new Integer( inContext.getRequestParameter( key ) );
					}
					else if ( parameterType.equals( long.class ) )
					{
						value = new Long( inContext.getRequestParameter( key ) );
					}
					else if ( parameterType.equals( double.class ) )
					{
						value = new Double( inContext.getRequestParameter( key ) );
					}
					else if ( parameterType.equals( byte.class ) )
					{
					    value = new Byte( inContext.getRequestParameter( key ) );
					}
					else if ( parameterTypes.equals( boolean.class ) )
					{
					    value = new Boolean( inContext.getRequestParameter( key ) );
					}
					else
					{
						continue;
					}
	
					method.invoke( inObject, new Object[] { value } );
				}
			}
		}
	}

}
