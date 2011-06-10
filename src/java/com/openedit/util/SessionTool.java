/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
/**
 * Provides utilities for instantiating classes out of templates.
 *
 * Company:      Deutsches Zentrum fuer Luft- und Raumfahrt
 * @author       Christoph.Reck@dlr.de
 * 
 * Elimintated some unused methods - Matt Avery, mavery@einnovation.com
 */
package com.openedit.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.openedit.ModuleManager;
import com.openedit.WebPageRequest;

/**
 * @author Matthew Avery, mavery@einnovation.com
 * 
 * This class was derived from a class I found on the Velocity
 * mailing list (see notes above) and adds some convenience methods for
 * use with Open Edit specific implementation.  It is still referenced
 * from Velocity pages as "$classtool".
 * 
 * December 4, 2004 - Check the Spring PluginManager first when constructing objects.
 */
public class SessionTool
{
	protected WebPageRequest fieldContext;
	protected ModuleManager fieldModuleManager;
	
	public SessionTool( WebPageRequest inContext, ModuleManager inManager)
	{
		fieldContext = inContext;
		fieldModuleManager = inManager;
	}
	
	public Object sessionInstance( String inSessionKey, String inClassName ) throws Exception
	{
		Object instance = getContext().getSessionValue( inSessionKey );
		if ( instance == null )
		{
			instance = construct( inSessionKey, inClassName );
			getContext().putSessionValue( inSessionKey, instance );
		}
		return instance;
	}

	protected WebPageRequest getContext()
	{
		return fieldContext;
	}
	
	public Object construct( String inKey, String inClassName ) throws Exception
	{
		if ( getModuleManager().contains( inKey ) )
		{
			return getModuleManager().getBean( inKey );
		}
		Class newClass = Class.forName( inClassName );
		Constructor[] constructors = newClass.getConstructors();
		for ( int i = 0; i < constructors.length; i++ )
		{
			Class[] argumentClasses = constructors[i].getParameterTypes();
			if ( argumentClasses.length == 0 )
			{
				return constructors[i].newInstance( null );
			}
			if ( argumentClasses.length == 1 
			  && argumentClasses[0].equals( WebPageRequest.class ) )
			{
				return constructors[i].newInstance( new Object[] { getContext() } );
			}
		}
		return newClass.newInstance();
	}
	
    /**
     * Instantiates a class by specifying its name (via empty constructor).
     *
     * @param className The name of the class to instantiates.
     * @return A new instance of the requested class.
     */
    public static Object newInstance(String className) throws Exception
    {
        Class cls = Class.forName(className);
        Class[] params =  new Class[0];

        try
        {
            Constructor constructor = cls.getConstructor(params);
            return constructor.newInstance( new Object[0] );
        } 
        catch (Exception ex) 
        {
            Constructor constructor = cls.getDeclaredConstructor(params);
            if ( Modifier.isPrivate( constructor.getModifiers() ) )
                return cls;  // class with static methods
        }
        return null;
    }

    /**
     * Convenience method which instantiates a class by specifying
     * its name and one parameter.
     *
     * @param className The name of the class to instantiates.
     * @param param A single parameters used to call the constructor.
     * @return A new instance of the requested class.
     */
    public static Object newInstance(String className, Object param)
           throws Exception
    {
        return newInstance( className, new Object[] {param} );
    }

    /**
     * Instantiates a class by specifying its name and parameters.
     *
     * @param className The name of the class to instantiates.
     * @param params Array of parameters used to call the constructor.
     * @return A new instance of the requested class.
     */
    public static Object newInstance(String className, Object[] params)
           throws Exception
    {
        Class cls = Class.forName(className);
        Constructor constructor = getConstructor(cls, params);
        return constructor.newInstance(params);
    }

    /**
     * Enhancement of the class objects own getConstructor() which takes
     * in consideration subclassing and primitives. The params
     * parameter is an array of objects that should be matched
     * classwise to the constructors formal parameter types, in declared
     * order. If params is null, it is treated as if it were an empty
     * array.
     *
     * @param cls        the class to search for a matching constructor
     * @param params     the array of parameters that will be used to
     *                   invoke the constructor
     * @return           the Method object of the public constructor that
     *                   matches the above
     * @see              java.lang.Class#getConstructor(Class[])
     **/
    public static Constructor getConstructor(Class cls, Object[] params)
    {
        Constructor[] constructors = cls.getConstructors();

        for (int i = 0; i < constructors.length; ++i )
        {
            Class[] parameterTypes = constructors[i].getParameterTypes();

            // The methods we are trying to compare must
            // the same number of arguments.
            if (parameterTypes.length == params.length)
            {
                // Make sure the given parameter is a valid
                // subclass of the method parameter in question.
                for (int j = 0; ; j++)
                {
                    if (j >= parameterTypes.length)
                        return constructors[i]; // found

                    Class c = parameterTypes[j];
                    Object p = params[j];
                    if ( c.isPrimitive() )
                    {
                        try
                        {
                            if ( c != p.getClass().getField("TYPE").get(p) )
                                break;
                        } catch (Exception ex) {
                            break; // p is not a primitive derivate
                        }
                    }
                    else if ( (p != null) &&
                              !c.isAssignableFrom( p.getClass() ) )
                        break;
                } // for all parameters
            } // if same length
        } // for all contructors

        return null;
    }
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
}
