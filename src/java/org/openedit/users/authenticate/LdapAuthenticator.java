/*
 * Created on Jul 20, 2006
 */
package org.openedit.users.authenticate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.users.User;
import org.openedit.users.UserManagerException;
import org.openedit.util.LDAP;

public class LdapAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(LdapAuthenticator.class);

	protected Map fieldServers;
	protected SearcherManager fieldSearcherManager;
	
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

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

    	Data server = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserver");
        if( server == null || server.get("value") == null)
        {
        	return false;
        }
        
        LDAP ldap = getServer(server.get("value"));
		
        Data prefix = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserverprefix");
        Data postfix = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserverpostfix");
        
		String inPassword = inAReq.getPassword();
		
		ldap.authenticate(prefix.get("value"),inUser.getUserName(), postfix.get("value"), inPassword);
		
		if (ldap.connect())
		{
			inUser.setEnabled(true);
			return true;
		}
		
		return false;
		
	}

}