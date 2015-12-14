/*
 * Created on Aug 2, 2006
 */
package org.openedit.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openedit.OpenEditRuntimeException;

public class XmlWriterPool
{
    protected ThreadLocal perThreadCache = new ThreadLocal();

    public XmlWriterPool() {
    }

    public void reset() {
        perThreadCache = new ThreadLocal();
    }

    public XMLWriter instance(String inEncoding) {
        Map ref = (Map) perThreadCache.get();
        if( ref == null)
        {
        	ref = new HashMap();
	    	// use weak reference to prevent cyclic reference during GC
            perThreadCache.set(ref);
        }
        XMLWriter singletonInstancePerThread = (XMLWriter)ref.get(inEncoding);
        
        if ( singletonInstancePerThread == null ) 
        {
        	OutputFormat format = OutputFormat.createPrettyPrint();
        	format.setEncoding(inEncoding); //this is annoying since we can only set it once per thread
        	
        	try
			{
				singletonInstancePerThread = new XMLWriter(format);
			}
			catch (UnsupportedEncodingException ex)
			{
				throw new OpenEditRuntimeException();
			}
        	ref.put(inEncoding, singletonInstancePerThread);	        	
        }
        return singletonInstancePerThread;
    }
}
	

