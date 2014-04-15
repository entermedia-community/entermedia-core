/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
 */

/*
 * Created on Jun 18, 2003
 *
 */
package com.openedit.util.strainer;

import java.util.Iterator;
import java.util.List;

import javax.naming.ConfigurationException;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.config.Configuration;

/**
 * DOCUMENT ME!
 * 
 * @author cburkey
 */
public class FilterReader {
	protected ModuleManager fieldModuleManager;

	/**
	 * Read the given configuration as a parent of a collection of filters.
	 * 
	 * @param inConfig
	 *            The configuration
	 * 
	 * @return A filter, or <code>null</code>, depending on the configuration
	 *         passed in:
	 *         <ul>
	 *         <li><code>null</code> if the configuration is null or has no
	 *         children;
	 *         <li>the corresponding filter, if the configuration has exactly
	 *         one child; or
	 *         <li>an <code>AndFilter</code> containing all the children, if
	 *         there is more than one child.
	 *         </ul>
	 * 
	 * @throws ConfigurationException
	 */
	public Filter readFilterCollection(Configuration inConfig,
			String inPermission) throws OpenEditException {
		Filter filter = null;

		if (inConfig != null) {
			List elements = inConfig.getChildren();
			switch (elements.size()) {
			case 0:
				// this means that this config is letting everything pass
				// filter = new BlankFilter();
				break;

			case 1:
				filter = readFilter((Configuration) elements.get(0),
						inPermission);

				break;

			default:

				Filter[] filters = new Filter[elements.size()];

				for (int i = 0; i < filters.length; i++) {
					filters[i] = readFilter((Configuration) elements.get(i),
							inPermission);
				}

				filter = new AndFilter(filters);

				break;
			}
		}

		return filter;
	}

	/**
	 * Read the given configuration, and deserialize it into a filter.
	 * 
	 * @param inConfig
	 *            The configuration
	 * 
	 * @return Filter The decoded filter
	 * 
	 * @throws ConfigurationException
	 *             If the configuration could not be decoded successfully
	 */
	protected Filter readFilter(Configuration inConfig, String inPermission)
			throws OpenEditException {
		if (inConfig == null) {
			return null;
		}

		// FIXME: Should make these into XML factories.
		Filter result = null;
		String elemName = inConfig.getName();
		List elements = inConfig.getChildren();
		if (elemName.equals("and")) {
			result = new AndFilter(readSubFilters(inConfig, inPermission));
		} else if (elemName.equals("or")) {
			result = new OrFilter(readSubFilters(inConfig, inPermission));
		} else if (elemName.equals("not")) {
			if (elements.size() > 1) {
				throw new OpenEditException(
						"<not> element must have one or less children");
			}
			if (elements.size() == 0) {
				result = new NotFilter();
			} else {
				result = new NotFilter(readFilter(
						(Configuration) elements.get(0), inPermission));
			}
		} else if (elemName.equals("user")) {
			result = new UserFilter(inConfig.getAttribute("name"));
		} else if (elemName.equals("userprofile")) {
			String name = inConfig.getAttribute("name");
			if (name == null) {
				name = inConfig.getAttribute("property");
			}
			String value = inConfig.getAttribute("value");
			if (value == null) {
				value = inConfig.getAttribute("equals");
			}
			result = new UserProfileFilter(name, value);
		} else if (elemName.equals("group")) {
			String id = inConfig.getAttribute("id");
			if (id == null) {
				id = inConfig.getAttribute("name");
			}
			result = new GroupFilter(id);
		} else if (elemName.equals("settingsgroup")) {
			String id = inConfig.getAttribute("id");
			if (id == null) {
				id = inConfig.getAttribute("name");
			}
			result = new SettingsGroupFilter(id);
		} else if (elemName.equals("permission")) {
			result = new PermissionFilter(inConfig.getAttribute("name"));
		} else if (elemName.equals("path")) {
			result = new PathFilter(inConfig.getAttribute("name"));
		} else if (elemName.equals("page-property")
				|| elemName.equals("pageproperty")) {
			result = new PagePropertyFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("equals"));
		} else if (elemName.equals("pagevalue")
				|| elements.equals("context-variable")) {
			result = new PageValueFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("equals"));
		}
		// not used much
		else if (elemName.equals("request-attribute")) {
			result = new RequestAttributeFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("equals"));
		} else if (elemName.equals("blank")) {
			result = new BlankFilter();
		} else if (elemName.equals("boolean")) {
			result = new BooleanFilter();
			result.setValue(inConfig.getAttribute("value"));
		} else if (elemName.equals("action")) {
			String target = inConfig.getAttribute("name");
			ActionFilter action = new ActionFilter(target, getModuleManager());
			action.setPermissionName(inPermission);
			if (inConfig.hasChildren()) // dont need this all the time
			{
				action.setConfiguration(inConfig);
			}
//			if (inConfig.getChildren("property") != null) {
//				for (Iterator iterator = inConfig.getChildIterator("property"); iterator
//						.hasNext();) {
//					Configuration config = (Configuration) iterator.next();
//					String key = config.get("id");
//					String value = config.getValue();
//					if (key != null && value != null) {
//						action.setProperty(key, value);
//					}
//
//				}
//			}
			result = action;
		} else if (elemName.equals("referer")) {
			String target = inConfig.getAttribute("value");
			RefererFilter filter = new RefererFilter(target);
			result = filter;
		} else if (elemName.equals("dataproperty")) {
			result = new DataPropertyFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("value"));
		} else if (elemName.equals("userproperty")) {
			result = new UserPropertyFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("value"));
		} else if (elemName.equals("groupproperty")) {
			result = new GroupPropertyFilter(inConfig.getAttribute("name"),
					inConfig.getAttribute("value"));
		}

		else // TODO: Look in Spring for some kind of filter
		{

			Filter filter = (Filter) getModuleManager().getBean(
					elemName + "Filter");
			if (filter != null) {
				filter.setConfiguration(inConfig);
				for (Iterator iterator = inConfig.getAttributeNames()
						.iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					String val = inConfig.getAttribute(key);
					if (val != null) {
						filter.setProperty(key, val);
					}

				}
				result = filter;
			} else {
				throw new OpenEditException("Unrecognized filter element <"
						+ elemName + ">");
			}
		}

		return result;
	}

	/**
	 * Read the children of the given configuration as an array of filters.
	 * 
	 * @param inConfig
	 *            The configuration whose children should be parsed
	 * 
	 * @return Filter[] The decoded filters
	 * 
	 * @throws ConfigurationException
	 *             If the configuration could not be decoded successfully
	 */
	protected Filter[] readSubFilters(Configuration inConfig,
			String inPermission) throws OpenEditException {
		List elements = inConfig.getChildren();
		Filter[] subFilters = new Filter[elements.size()];
		int index = 0;

		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			subFilters[index] = readFilter((Configuration) iter.next(),
					inPermission);
			index++;
		}

		return subFilters;
	}

	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}
}
