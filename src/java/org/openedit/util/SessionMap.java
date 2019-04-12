/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpSession;


/**
 * This class makes an <code>HttpSession</code> look like a <code>Map</code>. FIXME: Write a
 * testcase for this class!
 *
 * @author Eric Galluzzo
 */
public class SessionMap extends AbstractMap
{
	protected HttpSession fieldSession;

	public SessionMap(HttpSession inSession)
	{
		fieldSession = inSession;
	}

	/* (non-Javadoc)
	 * @see Map#isEmpty()
	 */
	public boolean isEmpty()
	{
		return getSession().getAttributeNames().hasMoreElements();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public HttpSession getSession()
	{
		return fieldSession;
	}

	/* (non-Javadoc)
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(Object key)
	{
		return (getSession().getAttribute((String) key) != null);
	}

	/* (non-Javadoc)
	 * @see Map#entrySet()
	 */
	public Set entrySet()
	{
		return new EntrySet();
	}

	/* (non-Javadoc)
	 * @see Map#get(Object)
	 */
	public Object get(Object key)
	{
		return getSession().getAttribute((String) key);
	}

	/* (non-Javadoc)
	 * @see Map#put(Object, Object)
	 */
	public Object put(Object key, Object value)
	{
		Object oldValue = getSession().getAttribute((String) key);
		getSession().setAttribute((String) key, value);

		return oldValue;
	}

	/* (non-Javadoc)
	 * @see Map#remove(Object)
	 */
	public Object remove(Object key)
	{
		Object oldValue = getSession().getAttribute((String) key);

		if (oldValue != null)
		{
			getSession().removeAttribute((String) key);
		}

		return oldValue;
	}

	/* (non-Javadoc)
	 * @see Map#size()
	 */
	public int size()
	{
		int size = 0;

		for (Enumeration e = getSession().getAttributeNames(); e.hasMoreElements();)
		{
			e.nextElement();
			size++;
		}

		return size;
	}

	/**
	 * This class implements an entry set as required by <code>{@link SessionMap#emptySet}</code>.
	 */
	protected class EntrySet extends AbstractSet
	{
		public EntrySet()
		{
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @return
		 */
		public boolean isEmpty()
		{
			return SessionMap.this.isEmpty();
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @return
		 */
		public Iterator iterator()
		{
			return new EntryIterator();
		}

		/* (non-Javadoc)
		 * @see Collection#remove(Object)
		 */
		public boolean remove(Object o)
		{
			return (SessionMap.this.remove(o) != null);
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @return
		 */
		public int size()
		{
			return SessionMap.this.size();
		}

		protected class EntryIterator implements Iterator
		{
			protected Enumeration fieldEnumeration = getSession().getAttributeNames();
			protected String fieldCurrentName;

			/**
			 * DOCUMENT ME!
			 *
			 * @return
			 */
			public boolean hasNext()
			{
				return fieldEnumeration.hasMoreElements();
			}

			/**
			 * DOCUMENT ME!
			 *
			 * @return
			 */
			public Object next()
			{
				fieldCurrentName = (String) fieldEnumeration.nextElement();

				if (fieldCurrentName == null)
				{
					return null;
				}
				else
				{
					return new org.openedit.util.SimpleEntry(
						fieldCurrentName, getSession().getAttribute(fieldCurrentName));
				}
			}

			/**
						 *
						 */
			public void remove()
			{
				getSession().removeAttribute(fieldCurrentName);
			}
		}
	}
}
