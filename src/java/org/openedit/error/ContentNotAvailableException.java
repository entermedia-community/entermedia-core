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
 * Created on Jul 11, 2003
 */
package org.openedit.error;

import org.openedit.OpenEditException;


/**
 * DOCUMENT ME!
 *
 * @author cburkey
 */
public class ContentNotAvailableException extends OpenEditException
{
	public ContentNotAvailableException(String inMsg, String inPath)
	{
		super(inMsg, inPath);
	}
}
