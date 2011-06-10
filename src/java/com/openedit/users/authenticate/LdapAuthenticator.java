/*
 * Created on Jul 20, 2006
 */
package com.openedit.users.authenticate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.users.User;
import com.openedit.users.UserManagerException;
import com.openedit.util.LDAP;

public class LdapAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(LdapAuthenticator.class);

	protected Map fieldServers;
	
	protected Map getServers()
	{
		if (fieldServers == null)
		{
			fieldServers = new HashMap();
		}

		return fieldServers;
	}
	
	protected LDAP getServer(String inServerName)
	{
		LDAP server = (LDAP)getServers().get(inServerName);
		if (server == null)
		{
			server = new LDAP(inServerName);
			getServers().put(inServerName, server);
		}
		return server;
	}
	
	
	public boolean authenticate(AuthenticationRequest inAReq) throws UserManagerException
	{
		User inUser = inAReq.getUser();

        String ldapserver = (String)inUser.getProperty("ldapserver");
        if( ldapserver == null)
        {
        	ldapserver = (String)inUser.getProperty("oe.ldapserver");	
        }
        
        if( ldapserver == null)
        {
        	return false;
        }
        
        LDAP ldap = getServer(ldapserver);
		
		String inPassword= inAReq.getPassword();
		ldap.authenticate(inUser, inPassword);
		
		if (ldap.connect())
		{
			return true;
		}
		
		return false;
		
	}

}