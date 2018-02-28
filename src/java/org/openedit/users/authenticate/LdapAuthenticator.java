/*
 * Created on Jul 20, 2006
 */
package org.openedit.users.authenticate;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.hittracker.HitTracker;
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
        
        //Set a user string
        String inUsername = inUser.getUserName();

        //Set a password string
        String inPassword = inAReq.getPassword();
        //log.info("Ldap Server Password:"+ inPassword);

        //Create a new Ldap instance
        LDAP ldap = new LDAP( server.get("value"));
        
        //log.info("New Ldap Init");

        //Lookup the DN
        Data searchdn = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserversearchdn");

        //Make a string from the result or null
        String searchDN = ( searchdn.get("value") != null ) ?  searchdn.get("value") : null;

        //log.info("New Ldap Search DN " + searchDN);

        //Lookup the field to search with
        Data searchfield = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserversearchfield");

        //Make a string from the result or null
        String searchField = ( searchfield.get("value") != null ) ?  searchfield.get("value") : null;

		//log.info("New Ldap Search Field " + searchField);

        //If the admin supplied a search field, we need to search ldap for the supplied value under this field and return the CN
        if( searchField != null)
        {
			//log.info("NOT NULL Search Field " + searchField);

			//We may need to tell the auth to go no auth, we can't trust the user has the read permissions for this operation.?
			ldap.setAuth("none");

			//log.info("Auth set to none");

			//If the search DN is set, set it
			if(searchDN != null) {
				ldap.setDN(searchDN);  //Is setdomain the same?, authenticate suggests not.
				ldap.setDomain(searchDN);
				log.info("Search DN Set");
			}

			//Search LDAP For the results
			HitTracker<?> searchResult = ldap.search( searchField, inUser.getUserName() );

			try {
				//Try to set the login username for LDAP, we will keep the supplied ID
				inUsername = searchResult.get(0).getValue("cn").toString();
			}catch(Exception e) {
				//No entries... some kind of error, or no results.
				log.error( "Unable to find user at LDAP server");
			}

			ldap = new LDAP( server.get("value"));

			//Return back to normal values for the bind
			//ldap.setAuth("simple");

			//log.info("Auth set to simple");
			//Authenticate will take care of resetting the DN

        }

        //Get the prefix for ldap binding
        Data prefix = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserverprefix");

		//Check the prefix and make a string
		String sprefix  = ( prefix != null ) ? prefix.get("value") : null;

        //log.info("New Ldap Prefix Field " + sprefix);

		//Check the post fix
        Data postfix = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserverpostfix");

        //Check the postfix and make a string
		String spostfix = ( postfix != null) ?  postfix.get("value") : null;

        //log.info("New Ldap Postfix Field " + spostfix);

		//Authenticate with Ldap
		ldap.authenticate(sprefix, inUsername, spostfix, inPassword);


		//If we have a connection
		if (ldap.connect())
		{
//	        Data usersearch = getSearcherManager().getData(inAReq.getCatalogId(), "catalogsettings", "ldapserverusersearch");
//	        if( usersearch != null)
//	        {
//	        	ldap.search("userdetails", inUser.getUserName());
//	        }
			
			if(!inUser.isEnabled() || inUser.isVirtual())
			{
				inUser.setEnabled(true);
				inUser.setVirtual(false);
				
				getSearcherManager().getSearcher(inAReq.getCatalogId(), "user").saveData(inUser);
			}
			
			return true;
		}
		
		return false;
		
	}

}
