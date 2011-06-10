package com.openedit.users.authenticate;

import com.openedit.users.Authenticator;
import com.openedit.users.User;
import com.openedit.users.UserManagerException;

public abstract class BaseAuthenticator implements Authenticator
{

	public boolean authenticate(User inUser, String inPassword) throws UserManagerException
	{
		AuthenticationRequest areq = new AuthenticationRequest();
		areq.setUser(inUser);
		areq.setPassword(inPassword);
		return authenticate(areq);
	}

}
