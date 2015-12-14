package org.openedit.generators;

import java.io.IOException;

import org.openedit.Generator;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.page.Page;

public class PipeLineGenerator extends BaseGenerator
{
	protected Generator fieldPipeLineGenerator;
	//This class is not finished yet
	
	public void generate(WebPageRequest inContext, Page inPage, Output inOut) throws OpenEditException
	{
		//Run the convert generator then read in the stream and apply a water mark?
		
		//Read in the stream and save it back out and apply a watermark?.. Seems dumb
		
		//Take this page and push it into the output
		//Loop over all the children pipeline generators along the way?
		PipeLineGenerator gen = createPipe(this);  //Like the stream API
		byte[] vals = new byte[1024];
		try
		{
		while( gen.read() != null )
		{
			inOut.getStream().write(vals);
		}
		} catch (IOException ex)
		{
			throw new OpenEditException(ex);
		}

	}

	protected byte[] read()
	{
		//get data from child
		//manipulae data
		//return data
		return null;
	}

	private PipeLineGenerator createPipe(PipeLineGenerator inPipeLineGenerator)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Generator getPipeLineGenerator()
	{
		return fieldPipeLineGenerator;
	}

	public void setPipeLineGenerator(Generator inPipeLineGenerator)
	{
		fieldPipeLineGenerator = inPipeLineGenerator;
	}

}
