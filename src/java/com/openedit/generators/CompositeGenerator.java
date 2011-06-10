/*
 * Created on Jul 21, 2004
 */
package com.openedit.generators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ReaderItem;

import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;
import com.openedit.page.PageStreamer;

/**
 * TODO: Just make this track a list of generators
 * @author Matthew Avery, mavery@einnovation.com
 */
public class CompositeGenerator extends BaseGenerator implements Generator
{
	protected List fieldAllGenerators;
	
	private static final Log log = LogFactory.getLog(CompositeGenerator.class);
	public CompositeGenerator(  )
	{
	}

	protected List getGenerators()
	{
		if ( fieldAllGenerators == null)
		{
			fieldAllGenerators =new ArrayList(2);
		}
		return fieldAllGenerators;
	}
	public void addGenerator(Generator inGen)
	{
		if ( inGen == null)
		{
			throw new OpenEditRuntimeException("No such generator");
		}
		getGenerators().add(inGen);
	}
	public void generate( WebPageRequest inContext, Page inPage, Output inOut ) throws OpenEditException
	{		
		PageStreamer pages = inContext.getPageStreamer();
		Output old = pages.getOutput(); //They might write out the streamer so we need to capture this
		Page resultsThuFar = inPage;

		//This class only support String composite generation. No byte[] stuff allowed
		
		Reader content = null;
		for (Iterator iter = getGenerators().iterator(); iter.hasNext();)
		{
			Generator gen = (Generator) iter.next();

			content = captureAllOutput(inContext, gen, resultsThuFar);
			//make fake page and keep processing
			resultsThuFar  = new Page( resultsThuFar );
			resultsThuFar.setContentItem( new ReaderItem( inPage.getPath(), content, inPage.getCharacterEncoding() ) );
		}
		
		try
		{
			pages.setOutput(old);
			getOutputFiller().fill(content,inOut.getWriter());
		//	inOut.getWriter().write(content);
			inOut.getWriter().flush();
		}
		catch (IOException ex)
		{
			log.error( ex );
		}
	}
	protected Reader captureAllOutput(WebPageRequest inContext, Generator inGen, Page inResultsThuFar ) throws OpenEditException
	{
		//We have to replace the streamer with one that has our context and writer
		
		//Cant copy because the action are editing the parent version
		WebPageRequest copy = inContext.copy();

		//This allows the $pages.stream method to keep working with our fake output
		PageStreamer streamer = inContext.getPageStreamer().copy();

		//allows capture of any output on our tmp streamer
		ByteArrayOutputStream scapture = new ByteArrayOutputStream();
		Writer capture = null;
		try
		{
			capture = new OutputStreamWriter(scapture, inResultsThuFar.getCharacterEncoding() );
			Output out = new Output(capture, scapture );
			streamer.setOutput(out);
			
			copy.putPageStreamer(streamer);
			streamer.setWebPageRequest(copy);
			
			//kicks the proces off without running any actions again
			inGen.generate( copy, inResultsThuFar , out);
	
			capture.flush();
			//String value = scapture.toString(inResultsThuFar.getCharacterEncoding());
			//return value;
			
			//Need a way to pull data from generators?
			return new InputStreamReader(new ByteArrayInputStream(scapture.toByteArray()),inResultsThuFar.getCharacterEncoding());
		}
		catch (IOException ex)
		{
			throw new OpenEditException(ex);
		}

	}

	public boolean canGenerate(WebPageRequest inReq)
	{
		for (Iterator iter = getGenerators().iterator(); iter.hasNext();)
		{
			Generator gen = (Generator) iter.next();
			if (  !gen.canGenerate( inReq ))
			{
				return false;
			}
		}
		return true;
	}

	public void setGenerators(List inMorefound)
	{
		fieldAllGenerators = inMorefound;
	}

	public boolean contains(Generator inClass)
	{
		return getGenerators().contains(inClass);
	}
	
	public boolean hasGenerator(Generator inChild)
	{
		if( inChild == this )
		{
			return true;
		}
		for (Iterator iter = getGenerators().iterator(); iter.hasNext();)
		{
			Generator gen = (Generator) iter.next();
			if ( gen.hasGenerator(inChild))
			{
				return true;
			}
		}
		return false;
	}

}
