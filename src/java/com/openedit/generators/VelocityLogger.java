/*
 * Created on Nov 11, 2004
 */
package com.openedit.generators;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class VelocityLogger implements LogSystem
{

	public VelocityLogger()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public void init( RuntimeServices arg0 ) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	public void logVelocityMessage( int inArg0, String inArg1 )
	{
			if( inArg1 != null )
			{
				if( inArg1.contains("Because it's not resetable" ) ||
						inArg1.contains("Velocimacro : added" ) )
				{
					return;			
				}
			}

		switch(inArg0){
			case DEBUG_ID:
				VelocityGenerator.log.debug(inArg1);
				break;
			case INFO_ID:
				VelocityGenerator.log.info(inArg1);
				break;
			case ERROR_ID:
				VelocityGenerator.log.error(inArg1);
				break;
			case WARN_ID:
				VelocityGenerator.log.warn(inArg1);
				break;
		}
		
	}

}
