package org.openedit.users.authenticate;

import org.openedit.users.Authenticator;
import org.openedit.users.User;
import org.openedit.users.UserManagerException;

public abstract class BaseAuthenticator implements Authenticator
{

	public boolean authenticate(String inCatalogId, User inUser, String inPassword) throws UserManagerException
	{
		AuthenticationRequest areq = new AuthenticationRequest();
		areq.setUser(inUser);
		areq.setCatalogId(inCatalogId);
		areq.setPassword(inPassword);
		return authenticate(areq);
	}

}
