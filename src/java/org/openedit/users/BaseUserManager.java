package org.openedit.users;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.event.EventManager;
import org.openedit.event.WebEvent;
import org.openedit.hittracker.HitTracker;
import org.openedit.users.authenticate.AuthenticationRequest;
import org.openedit.users.authenticate.PasswordGenerator;
import org.openedit.util.StringEncryption;

public class BaseUserManager implements UserManager
{
	private static final Log log = LogFactory.getLog(BaseUserManager.class);

	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected EventManager fieldEventManager;
	protected Authenticator fieldAuthenticator;
	protected Authenticator fieldTwoFactorAuthenticator;

	public Authenticator getTwoFactorAuthenticator() {
		if (fieldTwoFactorAuthenticator == null)
		{
			fieldTwoFactorAuthenticator = (Authenticator) getSearcherManager().getModuleManager().getBean(getCatalogId(), "twofactorAuthenticator");
			
		}

		return fieldTwoFactorAuthenticator;
	}

	public void setTwoFactorAuthenticator(Authenticator fieldTwoFactorAuthenticator) {
		this.fieldTwoFactorAuthenticator = fieldTwoFactorAuthenticator;
	}

	public void setAuthenticator(Authenticator inAuthenticator)
	{
		fieldAuthenticator = inAuthenticator;
	}

	protected EventManager getEventManager() 
	{
		return fieldEventManager;
	}

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public GroupSearcher getGroupSearcher()
	{
		return (GroupSearcher)getSearcherManager().getSearcher(getCatalogId(), "group");
	}

	
	public UserSearcher getUserSearcher()
	{
		return (UserSearcher)getSearcherManager().getSearcher(getCatalogId(), "user");
	}
	
	

	@Override
	public Group getGroup(String inGroupId) throws UserManagerException
	{
		return getGroupSearcher().getGroup(inGroupId);
	}

	@Override
	public HitTracker getGroups()
	{
		return getGroupSearcher().getAllHits();
	}

	@Override
	public User getUser(String inUserName) throws UserManagerException
	{
		return getUserSearcher().getUser(inUserName);
	}
	
	@Override
	public User getUser(String inUserName, boolean inFromCache)
	{
		if( inUserName == null)
		{
			return null;
		}
		return getUserSearcher().getUser(inUserName,inFromCache);
	}
	@Override
	public HitTracker getUsers()
	{

		return getUserSearcher().getAllHits();
	}
	public boolean authenticate(User inUser, String inPassword)
			throws UserManagerException {
		AuthenticationRequest req = new AuthenticationRequest();
		req.setUser(inUser);
		req.setPassword(inPassword);
		req.setCatalogId(getCatalogId());
		return authenticate(req);
	}
	public boolean authenticate(AuthenticationRequest inReq)
			throws UserManagerException {
		User inUser = inReq.getUser();

		if (!inUser.isEnabled()) {
			return false;
		}

		boolean success = getAuthenticator().authenticate(inReq);
		if (success && isTwoFactorEnabled()) 
		{
			success  = getTwoFactorAuthenticator().authenticate(inReq);
		}
		if (success) 
		{
			fireUserEvent(inUser, "login");
		}
		else 
		{
			fireUserEvent(inUser, "invalidpassword");
		}
		return success;
	}


	private boolean isTwoFactorEnabled() {
		Data data = getSearcherManager().getCachedData(getCatalogId(), "catalogsettings", "twofactorauthentication");
		if(data != null) {
			return Boolean.parseBoolean(data.get("value"));
		}
		else {
			return false;
		}
	}

	public Collection getGroupsSorted() {

		TreeSet treeSet = new java.util.TreeSet(new GroupComparator());

		treeSet.addAll(getGroups());

		return treeSet;
	}


	public String getScreenName(String inUserName) {
		if (inUserName == null) {
			return null;
		}
		User user = getUser(inUserName);
		if (user != null) {
			return user.getScreenName();
		}
		return inUserName;
	}

	public HitTracker getUsersInGroup(String inGroupId) 
	{
		return getUserSearcher().query().match("groups", inGroupId).search();
	}

	// TODO: Replace with smart UserHitTracker that lazy loads
	public HitTracker getUsersInGroup(Group inGroup) 
	{
		return getUsersInGroup(inGroup.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.users.UserManager#getUserByEmail(java.lang.String)
	 */
	public User getUserByEmail(String inEmail) throws UserManagerException {
		// check the cache first
		return getUserSearcher().getUserByEmail(inEmail);
	}
	/**
	 * @see org.openedit.users.UserManager#createGroup(String)
	 */
	public Group createGroup(String inGroupId, String inGroupName)
			throws UserManagerException {
			Group group = (Group)getGroupSearcher().createNewData();
			group.setId(inGroupId);
			group.setName(inGroupName);
			saveGroup(group);
			return group;
		}

	@Override
	public Group createGroup() throws UserManagerException
	{
	
		return (Group)getGroupSearcher().createNewData();
	}



	@Override
	public User createUser(String inUserName, String inPassword) throws UserManagerException
	{
		User user = (User)getUserSearcher().createNewData();
		if( inUserName != null)
		{
			user.setUserName(cleanUsername(inUserName));
		}
		user.setPassword(inPassword);
		saveUser(user);
		return user;
	}
	
	
	public String cleanUsername(String inUserName) {
		String cleanName = inUserName;
		cleanName = cleanName.trim();
		cleanName = cleanName.toLowerCase();
		
		cleanName = cleanName.replaceAll("[^A-Za-z0-9\\@\\-\\_\\.]", "");
		cleanName = cleanName.replace(' ','_');
	
		return cleanName;
	}
	
	@Override
	public void deleteGroup(Group inGroup) throws UserManagerException
	{
//		for (Iterator iter = listUserNames().iterator(); iter.hasNext();) {
//			String username = (String) iter.next();
//			User user = getUser(username);
//			user.removeGroup(inGroup);
//		}
//
		getGroupSearcher().delete(inGroup,null);
	}

	@Override
	public void deleteUser(User inUser) throws UserManagerException
	{
		fireUserEvent(inUser, "delete");
		getUserSearcher().delete(inUser, null);
	}

	public void deleteGroups(List inGroups) throws UserManagerException {
		if (inGroups != null) {
			for (Iterator iter = inGroups.iterator(); iter.hasNext();) {
				Group group = (Group) iter.next();
				deleteGroup(group);
			}
		}
	}

	public void deleteUsers(List inUsers) throws UserManagerException {
		if (inUsers != null) {
			for (Iterator iter = inUsers.iterator(); iter.hasNext();) {
				User user = (User) iter.next();
				deleteUser(user);
			}
		}
	}

	@Override
	public void saveUser(User inUser)
	{
		getUserSearcher().saveData(inUser,null);
	}

	@Override
	public void saveGroup(Group inGroup)
	{
		getGroupSearcher().saveData(inGroup,null);
	}

	///TODO: Refactor all the authentication to here
	@Override
	public Authenticator getAuthenticator()
	{

		return fieldAuthenticator;
	}

	@Override
	public StringEncryption getStringEncryption()
	{

		return getUserSearcher().getStringEncryption();
	}

	@Override
	public String encryptPassword(User inUser) throws OpenEditException
	{

		return getUserSearcher().encryptPassword(inUser);
	}

	@Override
	public String decryptPassword(User inUser) throws OpenEditException
	{
		return getUserSearcher().decryptPassword(inUser);
	}

	@Override
	public void setEventManager(EventManager inHandler)
	{
		fieldEventManager = inHandler;
	}

	@Override
	public void logout(User inUser)
	{
		fireUserEvent(inUser, "logout");
	}

	@Override
	public User createGuestUser(String inAccount, String inPassword, String inGroupId)
	{
			User user = (User)getUserSearcher().createNewData();
			user.setId(inAccount);
			user.setUserName(inAccount);
			user.setPassword(inPassword);
			user.setVirtual(true);
			user.setEnabled(true);

			Group group = getGroup(inGroupId);
			if (group == null) {
				log.error("No such auto login group " + inGroupId);
			} else {
				user.addGroup(group);
			}
			return user;
		}

	@Override
	public void flush()
	{
	}

	@Override
	public AuthenticationRequest createAuthenticationRequest(WebPageRequest inReq, String password, User user)
	{
		AuthenticationRequest aReq = new AuthenticationRequest();
		aReq.setUser(user);
		aReq.setCatalogId(getCatalogId());
		aReq.setPassword(password);

		String domain = inReq.getRequestParameter("domain");
		if (domain == null)
		{
			domain = inReq.getContentPage().get("authenticationdomain");
		}
		String []fields = inReq.getRequestParameters("field");
		if(fields != null) {
		for (int i = 0; i < fields.length; i++) {
			String key = fields[i];
			String value = inReq.getRequestParameter(key + ".value");
			if(value != null) {
				aReq.setValue(key, value);
			}
		}
		}
		aReq.putProperty("authenticationdomain", domain);
		String server = inReq.getPage().get("authenticationserver");
		aReq.putProperty("authenticationserver", server);
		return aReq;
	}

	public void fireUserEvent(User inUser, String inOperation) {
		if (fieldEventManager != null) {
			WebEvent event = new WebEvent();
			event.setOperation("authentication");
			event.setSearchType("user");
			event.setSource(this);
			event.addDetail("details", inOperation);
			event.setCatalogId(getCatalogId());
			event.setUser(inUser);
			event.setProperty("userid", inUser.getId());
			//event.setProperty("user", inUser.getId());

			getEventManager().fireEvent(event);
		}
	}

	@Override
	public String getEnterMediaKey(User user) 
	{
		String tempkey = getStringEncryption().getTempEnterMediaKey(user);
		return tempkey;
	}

	@Override
	public void logIntoApp(WebPageRequest inReq, User inUser)
	{
		String md5 = getStringEncryption().getPasswordMd5(inUser.getPassword());
		String value = inUser.getUserName() + "md542" + md5;
		inReq.putPageValue("entermediakey", value); //TODO: Remove this, its slow
		String catalogid = inReq.findValue("catalogid");
		inReq.putSessionValue(catalogid + "user", inUser);
		inReq.putPageValue("user", inUser);
		
	}

	@Override
	public String createNewTempLoginKey(String userid, String email, String first,String last)
	{
		//Create a new one for this user
		Searcher searcher = getSearcherManager().getSearcher("system", "templogincode");
		if( userid == null && first == null && last == null)
		{
			throw new OpenEditException("First or last name is required");
		}
		
		Data data  = searcher.createNewData();
		data.setValue("user",userid);
		data.setValue("firstName",first);
		data.setValue("lastName",last);
		data.setValue("email",email);
		data.setValue("date",new Date());
		
		String key = String.valueOf(new Random().nextInt(999999));
		data.setValue("securitycode",key);

		searcher.saveData(data);
		
		return key;
	}

	@Override
	public User checkForNewUser(String inEmail, String inTemplogincode, String groupid)
	{
		Searcher searcher = getSearcherManager().getSearcher("system", "templogincode");
		
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -5); //5 days
		Date newerthan = cal.getTime();
		Data found = searcher.query().exact("securitycode",inTemplogincode).after("date",newerthan).sort("date").searchOne();
		if( found != null)
		{
			String email = found.get("email");
			if( inEmail.equalsIgnoreCase(email))
			{
				//Must be valid user
				String tmppassword = new PasswordGenerator().generate();
				User user = createGuestUser(null, tmppassword, groupid);
				user.setVirtual(false);
				user.setEnabled(true);
				user.setValue("firstName",found.get("firstName"));
				user.setValue("lastName",found.get("lastName"));
				user.setEmail(found.get("email"));
				saveUser(user);
				found.setValue("user",user.getId());
				searcher.saveData(found);
				log.info("Temporary user made for " + inEmail);
				return user;
			}
			else
			{
				log.error("Invalid email " + inEmail);
			}
		}
		else
		{
			log.error("User not found " + inEmail);
		}
		return null;
	}



}
