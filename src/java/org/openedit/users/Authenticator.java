/*
 * Created on Jul 10, 2006
 */
package org.openedit.users;

import org.openedit.users.authenticate.AuthenticationRequest;

public interface Authenticator
{
	public boolean authenticate(String inCatalogId, User inUser, String inPassword)	throws UserManagerException;

	public boolean authenticate(AuthenticationRequest inReq)	throws UserManagerException;

}
