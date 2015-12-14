package org.openedit.users.authenticate;

import java.util.Iterator;
import java.util.List;

import org.openedit.users.Authenticator;
import org.openedit.users.UserManagerException;

public class CompositeAuthenticator extends BaseAuthenticator
{
	protected List fieldAuthenticators;
	
	public boolean authenticate(AuthenticationRequest inAReq) throws UserManagerException
	{

		for (Iterator iterator = getAuthenticators().iterator(); iterator.hasNext();)
		{
			Authenticator authen = (Authenticator) iterator.next();
			if( authen.authenticate(inAReq))
			{
				return true;
			}
		}
		return false;
	}

	public List getAuthenticators()
	{
		return fieldAuthenticators;
	}

	public void setAuthenticators(List inAuthenticators)
	{
		fieldAuthenticators = inAuthenticators;
	}

}
