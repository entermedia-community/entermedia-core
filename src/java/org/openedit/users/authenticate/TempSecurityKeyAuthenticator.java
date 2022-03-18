package org.openedit.users.authenticate;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.users.UserManagerException;

public class TempSecurityKeyAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(TempSecurityKeyAuthenticator.class);

	ModuleManager fieldModuleManager;
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public boolean authenticate(AuthenticationRequest inAReq) throws UserManagerException
	{
		User user = inAReq.getUser();
		String code = inAReq.get("templogincode");
		
		if( code == null)
		{
			return false;
		}
		//Search for the code
		UserManager userManager = getUserManager(inAReq.getCatalogId());
		
		Searcher searcher = getSearcherManager().getSearcher("system", "templogincode");
		
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1); //24 hours
		Date newerthan = cal.getTime();
		Data found = searcher.query().exact("user",user.getId()).exact("securitycode",code).after("date",newerthan).searchOne();
		if( found != null)
		{
			String securitycode = found.get("securitycode");  //Double checking
			if( code.equals(securitycode))
			{
				return true;
			}
		}		
		return false;
	}

	protected SearcherManager getSearcherManager()
	{
		return (SearcherManager)getModuleManager().getBean("searcherManager");
	}

	private UserManager getUserManager(String inCatalogId)
	{
		if(inCatalogId != null) {
			return  (UserManager) getModuleManager().getBean( inCatalogId, "userManager");

		} else {
			return  (UserManager) getModuleManager().getBean( "userManager");
		}
	}
	

}
