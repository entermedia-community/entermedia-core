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
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.openedit;

import org.openedit.generators.Output;
import org.openedit.page.Page;




/**
 * A generator is responsible for rendering content (HTML, PDF, etc.) back to the client.
 *
 * @author Chris Burkey
 */
public interface Generator
{
	/**
	 * This method should retrieve the response from the context and write content to it based on
	 * the information in the page and its generator data (which will have come from this
	 * generator).
	 *
	 * @param inContext The context for the given page
	 * @param inGenConfig The generator's configuration for the given page
	 * @param inOutput The output stream to which to write output
	 //FIXME
	 */
	public void generate( WebPageRequest inContext, Page inPage, Output inOut ) throws OpenEditException;
	public String getName();
	public void setName(String inName);
	public boolean canGenerate(WebPageRequest inReq);
	public boolean hasGenerator(Generator inChild);
}
