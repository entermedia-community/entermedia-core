/*
 * Created on Aug 10, 2003
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
package org.openedit.repository;

import java.util.List;
import java.util.Map;



/**
 * This is a generic interface to store web content, e.g HTML, XML, images
 * or other documents.
 * 
 * @author Matt Avery, mavery@einnovation.com
 */
public interface Repository
{
	boolean matches(String inPath);
	/*
	 * This should never return null, it can return a blank ContentItem though.
	 */
	ContentItem get( String inPath ) throws RepositoryException;
	ContentItem getStub( String inPath ) throws RepositoryException;
	
	boolean doesExist( String inPath) throws RepositoryException;
	
	void put( ContentItem inContent ) throws RepositoryException;
	
	void copy( ContentItem inSource, ContentItem inDestination ) throws RepositoryException;

	void move( ContentItem inSource, ContentItem inDestination ) throws RepositoryException;
	
	void move( ContentItem inSource, Repository inSourceRepository, ContentItem inDestination ) throws RepositoryException;

	
	void remove( ContentItem inPath ) throws RepositoryException;
	
	/* Returns a List of ContentItems sorted by version number
	 * 
	 */
	List getVersions( String inPath ) throws RepositoryException;
	
	public ContentItem getLastVersion(String inPath) throws RepositoryException;
	
	void setPath( String inPath);
	String getPath();
	/**
	 * This is the external URL root full path that is saved in the configuration.
	 * 
	 * @param inRootAbsolutePath
	 */
	void setExternalPath(String inRootAbsolutePath);
	String getExternalPath();
	
	void setFilterIn(String inFilters);
	void setFilterOut(String inFilters);

	String getFilterIn();
	String getFilterOut();

	List getChildrenNames(String inParent) throws RepositoryException;
	void deleteOldVersions(String inPath) throws RepositoryException;
	
	String getRepositoryType();
	void setRepositoryType(String inType);
	
	public String getMatchesPostFix();
	public void setMatchesPostFix(String inMatchesPostFix);
	public void setProperty(String inPropName, String inValue);
	public String getProperty(String inPropName);
	public Map getProperties();
	
}
