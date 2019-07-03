package org.openedit.users;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.data.SearcherManager;
import org.openedit.event.EventManager;
import org.openedit.event.WebEvent;
import org.openedit.hittracker.HitTracker;
import org.openedit.users.authenticate.AuthenticationRequest;
import org.openedit.util.StringEncryption;

public class BaseUserManager implements UserManager
{
	private static final Log log = LogFactory.getLog(BaseUserManager.class);

	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected EventManager fieldEventManager;
	protected Authenticator fieldAuthenticator;
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
		if (success) {
			fireUserEvent(inUser, "login");
		} else {
			fireUserEvent(inUser, "invalidpassword");
		}
		return success;
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
			user.setUserName(inUserName);
		}
		user.setPassword(inPassword);
		saveUser(user);
		return user;
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
			event.setProperty("user", inUser.getId());

			getEventManager().fireEvent(event);
		}
	}



}
