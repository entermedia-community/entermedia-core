package org.openedit.event;

import java.util.ArrayList;
import java.util.Collection;

import org.openedit.util.PathUtilities;

public class ListenerFilter
{
	String[] fieldActions;
	WebEventListener fieldListener;
	public String[] getActions()
	{
		return fieldActions;
	}
	public void setActions(String[] inActions)
	{
		fieldActions = inActions;
	}
	public WebEventListener getListener()
	{
		return fieldListener;
	}
	public void setListener(WebEventListener inListener)
	{
		fieldListener = inListener;
	}
	public boolean shouldListen(String inAction)
	{
		for (int i = 0; i < fieldActions.length; i++)
		{
			if(fieldActions[i].equals(inAction))
			{
				return true;
			}
			if( PathUtilities.match(inAction, fieldActions[i]) )
			{
				return true;
			}
		}
		
		return false;
	}
}
