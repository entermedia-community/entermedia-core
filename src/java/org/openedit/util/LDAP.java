package org.openedit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.data.BaseData;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.ListHitTracker;
import org.openedit.users.User;
import org.openedit.users.UserManagerException;

public class LDAP
{
	private static final Log log = LogFactory.getLog(LDAP.class);
	protected Properties fieldEnvironment;
	protected DirContext fieldContext;
	protected String fieldServerName;
	protected String fieldDomain;
	protected int fieldMaxLdapResults;
	
	public LDAP()
	{
		if (getServerName() != null)
		{
			init(getServerName());
		}
	}

	public LDAP(String inServerName)
	{
		init(inServerName);
	}
	
	public void setServerName(String inServerName)
	{
		init(inServerName);
		fieldServerName = inServerName;
	}

	public String getServerName()
	{
		return fieldServerName;
	}

	protected Properties getEnvironment()
	{
		if (fieldEnvironment == null)
		{
			fieldEnvironment = new Properties();
		}

		return fieldEnvironment;
	}

	protected void setEnvironment(Properties inEnvironment)
	{
		fieldEnvironment = inEnvironment;
	}

	public void init(String inServerName)
	{
		Properties env = getEnvironment();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		if (!inServerName.startsWith("ldap"))
		{
			throw new OpenEditException("ldapserver value must start with ldap");
		}
		env.put(Context.PROVIDER_URL, inServerName);	
		log.info("[LDAP] Connecting to " + inServerName);
	}
	public void authenticate(User inUser, String inPassword)
	{
		String prefix = (String) inUser.get("ldap_prefix");
		String post = (String) inUser.get("ldap_postfix");
		authenticate(prefix, inUser.getUserName(), post, inPassword);

	}
	public void authenticate(String prefix, String username, String postfix, String inPassword)
	{
		StringBuffer dnbuffer = new StringBuffer();
		if (prefix != null)
		{
			dnbuffer.append(prefix);
		}
		dnbuffer.append(username);
		if (postfix != null)
		{
			//dnbuffer.append(",");
			dnbuffer.append(postfix);
		}
		//uid=%s,ou=People,dc=company,dc=com
		// String base = "ou=People,dc=example,dc=com";
		// String dn = "uid=" + inUser.getUserName() + "," + base;

		getEnvironment().put(Context.SECURITY_PRINCIPAL, dnbuffer.toString());
		getEnvironment().put(Context.SECURITY_CREDENTIALS, inPassword);

	}

	public DirContext getContext()
	{
		return fieldContext;
	}

	public boolean connect() throws UserManagerException
	{
		if (isConnected())
		{
			return false;
		}

		try
		{
			// obtain initial directory context using the environment
			fieldContext = new InitialDirContext(getEnvironment());

			// now, create the root context, which is just a subcontext
			// of this initial directory context.
			// ctx.createSubcontext( rootContext );
		} catch (NameAlreadyBoundException nabe)
		{
			log.error("LDAP has already been bound!");
			return false;
		} catch (Exception e)
		{
			log.error(e);
			return false;
		}
		return true;
	}

	public void disconnect()
	{
		if (isConnected())
		{
			try
			{
				getContext().close();
				fieldContext = null;
			} catch (NamingException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isConnected()
	{
		if (getContext() != null)
		{
			try
			{
				getContext().lookup("");
				return true;
			} catch (NamingException ne)
			{
				return false;
			}
		}
		return false;
	}

	/**
	 * These searches do not take in a list of attributes
	 * @param domain
	 * @param searchby
	 * @param value
	 * @return
	 */
	public HitTracker search(String searchby, String value)
	{
		return search(searchby, null, value);
	}
	public HitTracker search(String searchby,  String operation, String value)
	{
		String query = null;
		if(operation != null && operation.equals("substring"))
		{
			query = "(" + searchby + "=" + value + "*)";
		}
		else
		{
			query = "(" + searchby + "=" + value + ")";
		}
		return search(getDomain(), query, getMaxLdapResults());
	}			

	/**
	 * Gets ALL the known Attributes
	 * @param domain
	 * @param query
	 * @param maxresults: maximum results returned.  Use 1 for lookups.
	 * @return
	 */
	protected HitTracker search(String query)
	{
		return search( getDomain(), query, getMaxLdapResults());
	}
	
	protected HitTracker search(String domain, String query, int maxresults)
	{
		connect();
		
		if (domain == null)
		{
			domain = "";
		}

		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctrl.setCountLimit(maxresults);

		NamingEnumeration enumeration;
		ListHitTracker results = new ListHitTracker(); //list of HashMaps
		try
		{
			enumeration = getContext().search(domain, query, ctrl);

			while (enumeration.hasMore())
			{
				SearchResult result = (SearchResult) enumeration.next();
				Attributes attribs = result.getAttributes();
				NamingEnumeration values = attribs.getAll();
				Map row = new HashMap();
				while( values.hasMore())
				{
					BasicAttribute basic = (BasicAttribute) values.next();
					String value = basic.get().toString();
					row.put(basic.getID(), value);
				}
				results.add(new BaseData(row));
				if( results.size() == maxresults )
				{
					break;
				}
			}
			log.info("[LDAP] Searched for " + query + " in domain " + domain  + " found " +  results.size());

		}
		catch (SizeLimitExceededException e)
		{
			log.error(e);
		}
		catch (PartialResultException e)
		{
			//more than 1
			log.error(e);
		}
		catch (NamingException e)
		{
			throw new OpenEditException(e);
		}

		return results;
	}

	public String getDomain()
	{
		return fieldDomain;
	}

	public void setDomain(String inDomain)
	{
		fieldDomain = inDomain;
	}

	public int getMaxLdapResults()
	{
		return fieldMaxLdapResults;
	}

	public void setMaxLdapResults(int inMaxLdapResults)
	{
		fieldMaxLdapResults = inMaxLdapResults;
	}
}
