/*
 * Created on Jul 10, 2006
 */
package com.openedit.users;

import com.openedit.users.authenticate.AuthenticationRequest;

public interface Authenticator
{
	public boolean authenticate(User inUser, String inPassword)	throws UserManagerException;

	public boolean authenticate(AuthenticationRequest inReq)	throws UserManagerException;

}
