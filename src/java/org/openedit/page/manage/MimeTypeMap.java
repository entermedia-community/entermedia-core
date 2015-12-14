/*
 * Created on May 15, 2006
 */
package org.openedit.page.manage;

/**
 * <p>
 * Interface for a map to determine the MIME type for a file or path
 * based upon its extension.  This interface was extracted from the
 * class of the same name originally from JPublish (http://www.jpublish.org)
 * and adapted by Open Edit (http://www.openedit.org).
 * </p>
 * 
 * @author Eric Broyles <eric@sandra.com>
 * @version $Id: MimeTypeMap.java,v 1.11 2006/05/22 18:07:32 ebroyles Exp $
 */
public interface MimeTypeMap
{

    /**
     * Set the default MIME type.
     *
     * @param inMimeType The new default MIME type.  If null, the DEAFULT_MIME_TYPE is used.
     */
    public void setDefaultMimeType(String inMimeType);

    /**
     * Get the default MIME type.
     *
     * @return The default MIME type.
     */
    public String getDefaultMimeType();

    /**
     * Get the MIME type for the given file extension.
     *
     * @param inExtension The extension to map to a MIME type.
     *
     * @return The MIME type
     */
    public String getMimeType(String inExtension);

    /**
     * Get the MIME type for the given path.
     *
     * @param path The path to map the a MIME type.
     *
     * @return The MIME type.
     */
    public String getPathMimeType(String path);

    public Object get(Object key);

    public Object put(Object arg0, Object arg1);

}