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
 * Created on Apr 18, 2003
 *
 */
package com.openedit;

import com.openedit.page.Page;


/**
 * DOCUMENT ME!
 *
 * @author cburkey
 */
public interface PageAccessListener
{
	/**
	 * DOCUMENT ME!
	 *
	 * @param inPage
	 * @param inRevision
	 *
	 * @throws OpenEditException
	 */
	public void pageAdded(Page inPage);

	/**
	 * DOCUMENT ME!
	 *
	 * @param inPage
	 * @param inRevision
	 *
	 * @throws OpenEditException
	 */
	public void pageModified(Page inPage);

	/**
	 * DOCUMENT ME!
	 *
	 * @param inPage
	 * @param inRevision
	 *
	 * @throws OpenEditException
	 */
	public void pageRemoved(Page inPage);

	/**
	 * DOCUMENT ME!
	 *
	 * @param inPage
	 *
	 * @throws OpenEditException
	 */
	public void pageRequested(Page inPage);
}
