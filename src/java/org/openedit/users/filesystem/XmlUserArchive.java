/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
 */

package org.openedit.users.filesystem;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.openedit.OpenEditException;
import org.openedit.event.WebEvent;
import org.openedit.event.WebEventHandler;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.ListHitTracker;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.repository.ContentItem;
import org.openedit.users.Authenticator;
import org.openedit.users.DuplicateGroupException;
import org.openedit.users.DuplicateUserException;
import org.openedit.users.Group;
import org.openedit.users.GroupComparator;
import org.openedit.users.User;
import org.openedit.users.UserComparator;
import org.openedit.users.UserManagerException;
import org.openedit.users.UserNotEnabledException;
import org.openedit.users.authenticate.AuthenticationRequest;
import org.openedit.users.authenticate.PasswordGenerator;
import org.openedit.util.DateStorageUtil;
import org.openedit.util.IntCounter;
import org.openedit.util.PathUtilities;
import org.openedit.util.StringEncryption;
import org.openedit.util.XmlUtil;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

/**
 * This user manager stores its users as files in a directory. Note that clients
 * of this user manager must set the user and group directories, then call
 * {@link #load()}.
 *
 * @author Eric and Matt
 *
 * @see #setUserDirectory(File)
 * @see #setGroupDirectory(File)
 * @see #load()
 */
public class XmlUserArchive  {
	protected String fieldCatalogId;

	public String getCatalogId() {
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId) {
		fieldCatalogId = inCatalogId;
	}

	//protected String fieldGroupDirectory;
	//protected String fieldUserDirectory;
	protected IntCounter fieldUserIdCounter;
	protected Map fieldGroupIdToGroupMap;
	protected Map fieldUserNameToUserMap;
	protected XmlArchive fieldXmlArchive;

	protected long fieldLastEditTime;
	protected Authenticator fieldAuthenticator;
	protected StringEncryption fieldStringEncryption;

	protected boolean fieldAllUsersLoaded = false;
	protected PageManager fieldPageManager;
	protected WebEventHandler fieldWebEventHandler;
	protected PermissionsManager fieldPermissionsManager;
	protected XmlUtil fieldXmlUtil;
	private static final Log log = LogFactory
			.getLog(XmlUserArchive.class);
	private static final int MAX_LOGIN_FAILS = 5;

	/**
	 * Constructor for FileSystemUserManager.
	 */
	public XmlUserArchive() {
		super();
	}

//	public FileSystemUserManager(String inUserDirectory, String inGroupDirectory)
//			throws UserManagerException {
//		setUserDirectory(inUserDirectory);
//		setGroupDirectory(inGroupDirectory);
//	}

	public List getPermissions() throws UserManagerException {
		return getPermissionsManager().getSystemPermissions();
	}

	public List getSystemPermissionGroups() {
		return getPermissionsManager().getSystemPermissionGroups();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openedit.users.filesystem.FileSystemUserLocator#getFileSystemUser
	 * (String)
	 */
	public FileSystemUser getFileSystemUser(String inUserName)
			throws UserManagerException {
		return (FileSystemUser) getUser(inUserName);
	}

	/**
	 * @see org.openedit.users.UserManager#getGroup(String)
	 */
	public Group getGroup(String inGroupId) {
		Group group = (Group) getGroupIdToGroupMap().get(inGroupId);
		File find = loadGroupFile(inGroupId);
		if (group != null) {
			if (group.getLastModified() == find.lastModified()) {
				return group;
			}

		}
		if (!find.exists()) {
			ContentItem stub = getPageManager().getRepository().getStub(
					"/WEB-INF/groups/" + inGroupId + ".xml");
			find = new File(stub.getAbsolutePath());
		}
		if (!find.exists()) {
			return null;
		}

		if (group == null) {
			// System.out.println("calling " + inGroupId + " " + hashCode() );
			group = new FileSystemGroup();
		}
		FileSystemGroup loadgroup = (FileSystemGroup) group;
		group = loadgroup;
		loadgroup.setLastModified(find.lastModified());
		getGroupIdToGroupMap().put(inGroupId, group);

		Element root = getXmlUtil().getXml(find, "UTF-8");
		loadgroup.setId(root.attributeValue("id"));
		if (loadgroup.getId() == null) {
			loadgroup.setId(inGroupId);
		}
		loadgroup.setName(root.elementText("group-name"));
		if (loadgroup.getName() == null) {
			loadgroup.setName(inGroupId);
		}
		Element perm = root.element("permissions");
		if (perm != null) {
			for (Iterator iterator = perm.elementIterator("permission"); iterator
					.hasNext();) {
				Element type = (Element) iterator.next();
				loadgroup.addPermission(type.getTextTrim());
			}
		}
		MapPropertyContainer properties = new MapPropertyContainer();
		Element props = root.element("properties");
		properties.loadProperties(props);
		loadgroup.setPropertyContainer(properties);

		return loadgroup;

	}

	/**
	 * Sets the directory in which all the group XML files reside.
	 *
	 * @param groupDirectory
	 *            The new group directory
	 */
//	public void setGroupDirectory(String groupDirectory) {
//		fieldGroupDirectory = groupDirectory;
//	}

	/**
	 * Returns the directory in which all the group XML files reside.
	 *
	 * @return File
	 */
	public String getGroupDirectory() {
		return "/WEB-INF/data/" + getCatalogId() + "/groups";
	}

	/**
	 * @see org.openedit.users.UserManager#getGroups()
	 */
	public HitTracker getGroups() {
		Collection ids = listGroupIds();
		TreeSet treeSet = new java.util.TreeSet(new GroupComparator());
		for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			treeSet.add(getGroup(id));
		}
		ListHitTracker list = new ListHitTracker(new ArrayList(treeSet));
		list.setHitsName("groups");
		list.setCatalogId(getCatalogId());
		return list;
	}

	public Collection getGroupsSorted() {

		TreeSet treeSet = new java.util.TreeSet(new GroupComparator());

		treeSet.addAll(getGroups());

		return treeSet;
	}

	/**
	 * @see org.openedit.users.UserManager#getUser(String)
	 */
	public User getUser(String inUserName) {
		if (inUserName == null) {
			return null;
		}
		User user = (User) getUserNameToUserMap().get(inUserName);
		if (user == null) {
			user = loadUser(inUserName);
		}
		return user;
	}

	/**
	 * Sets the directory in which all the user XML files reside.
	 *
	 * @param userDirectory
	 *            The new user directory
	 */
//	public void setUserDirectory(String userDirectory) {
//		fieldUserDirectory = userDirectory;
//	}

	/**
	 * Returns the directory in which all the user XML files reside.
	 *
	 * @return File
	 */
	public String getUserDirectory() {
		return "/WEB-INF/data/" + getCatalogId() + "/users";
	}

	public void setPageManager(PageManager pageManager) {
		fieldPageManager = pageManager;
	}

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	/**
	 * @see org.openedit.users.UserManager#getUsers()
	 */
	public HitTracker getUsers() {
		List col = listUserNames();
		HitTracker tracker = new ListHitTracker();
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			User user = getUser(id);
			if (id != null) {
				tracker.add(user);
			}
		}

		return tracker;
	}

	public List listUserNames() {
		List all = new ArrayList();
		ContentItem item = getPageManager().getRepository().get(
				getUserDirectory());
		File users = new File(item.getAbsolutePath());

		File[] files = users.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String username = PathUtilities.extractPageName(files[i]
						.getName());
				all.add(username);
			}
		}

//		// Temporary
//		item = getPageManager().getRepository().get("/WEB-INF/users");
//		users = new File(item.getAbsolutePath());
//
//		files = users.listFiles(new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				return name.endsWith(".xml");
//			}
//		});
//		if (files != null) {
//			for (int i = 0; i < files.length; i++) {
//				String username = PathUtilities.extractPageName(files[i]
//						.getName());
//				if (!all.contains(username)) {
//					all.add(username);
//				}
//			}
//		}

		return all;
	}

	/**
	 * Move this API to the UserManager
	 * @see org.openedit.users.UserManager#authenticate(User, String)
	 */
	public boolean authenticate(User inUser, String inPassword)
			throws UserManagerException {
		AuthenticationRequest req = new AuthenticationRequest();
		req.setUser(inUser);
		req.setPassword(inPassword);
		return authenticate(req);
	}

	public boolean authenticate(AuthenticationRequest inReq)
			throws UserManagerException {
		User inUser = inReq.getUser();

		if (!inUser.isEnabled()) {
			throw new UserNotEnabledException();
		}

		boolean success = getAuthenticator().authenticate(inReq);
		if (success) {
			fireUserEvent(inUser, "login");
		} else {
			fireUserEvent(inUser, "invalidpassword");
		}
		return success;
	}

	/**
	 * @see org.openedit.users.UserManager#createGroup(String)
	 */
	public Group createGroup(String inGroupId, String inGroupName)
			throws UserManagerException {
		if (getGroupIdToGroupMap().containsKey(inGroupId)) {
			throw new DuplicateGroupException(
					"A group already exists with the id \"" + inGroupId + "\"");
		} else {
			// File file = createGroupFile(inGroupName);
			FileSystemGroup group = new FileSystemGroup();
			group.setId(inGroupId);
			group.setName(inGroupName);
			getGroupIdToGroupMap().put(inGroupId, group);
			saveGroup(group);
			return group;
		}
	}

	public Group createGroup(String inGroupId) throws UserManagerException {
		return createGroup(inGroupId, inGroupId);
	}

	public Group createGroup() throws UserManagerException {
		return new FileSystemGroup();
	}

	/**
	 * @see org.openedit.users.UserManager#createUser(String, String)
	 */
	public User createUser(String inUserName, String inPassword)
			throws UserManagerException {
		if (inUserName == null) // when null then we are picking up the next
								// user from the list. May be dup username
		{
			int id = getUserIdCounter().incrementCount();
			inUserName = String.valueOf(id);
			User user = getUser(inUserName);
			if (user != null) {
				return user;
			}
		} else if (getUser(inUserName) != null) {
			throw new DuplicateUserException(
					"A user already exists with the username \"" + inUserName
							+ "\"");
		}

		if (inPassword == null || inPassword.length() <= 0) {
			inPassword = new PasswordGenerator().generate();// Integer.toString((int)(100000
															// +
															// generator.nextDouble()
															// * 899999D));
		}
		inPassword = encrypt(inPassword);

		createUserFile(inUserName, inPassword);
		User user = getUser(inUserName);
		fireUserEvent(user, "add");
		return user;
	}

	/**
	 * May be subclassed
	 */
	public User loadUser(String inUserName) throws UserManagerException {
		File userFile = loadUserFile(inUserName);
		if (!userFile.exists()) {
			ContentItem stub = getPageManager().getRepository().getStub(
					"/WEB-INF/users/" + inUserName + ".xml");
			userFile = new File(stub.getAbsolutePath());
		}
		if (!userFile.exists()) {
			return null;
		}

		FileSystemUser user = new FileSystemUser();
		user.setUserName(inUserName);

		Element root = getXmlUtil().getXml(userFile, "UTF-8");

		user.setEnabled(true);
		// String enabled = root.attributeValue("enabled");
		// if (enabled != null && Boolean.parseBoolean(enabled) == false)
		// {
		// user.setEnabled(false);
		// }
		// else
		// {
		// user.setEnabled(true);
		// }

		Element passwordElem = root.element("password");
		if (passwordElem != null) {
			user.setPassword(passwordElem.getText());
		}

		Element lastLoginElem = root.element("lastLogined-Time");
		if (lastLoginElem != null) {
			user.setLastLoginTime(lastLoginElem.getText());
		}

		Element creationDateElem = root.element("creation-date");
		if (creationDateElem != null) {
			long time = Long.parseLong(creationDateElem.getText());
			user.setCreationDate(new Date(time));
		}
		MapPropertyContainer container = new MapPropertyContainer();
		container.loadProperties(root.element("properties"));
		user.setPropertyContainer(container);

		for (Iterator iter = root.elementIterator("group"); iter.hasNext();) {
			Element group = (Element) iter.next();
			Group g = getGroup(group.attributeValue("id"));
			if (g != null) {
				user.addGroup(g);
			} else {
				log.error("Missing group " + group.attributeValue("id"));
			}
		}
		// TODO :Replace with cache
		getUserNameToUserMap().put(user.getUserName(), user);

		return user;
	}

	/**
	 * @see org.openedit.users.UserManager#deleteGroup(Group)
	 */
	public void deleteGroup(Group inGroup) throws UserManagerException {
		for (Iterator iter = listUserNames().iterator(); iter.hasNext();) {
			String username = (String) iter.next();
			User user = getUser(username);
			user.removeGroup(inGroup);
		}
		File file = loadGroupFile(inGroup.getId());
		file.delete();
		getGroupIdToGroupMap().remove(inGroup.getId());
		Page item = getPageManager().getPage(
				"/WEB-INF/groups/" + inGroup.getId() + ".xml");
		getPageManager().removePage(item);

	}

	/**
	 * @see org.openedit.users.UserManager#deleteUser(User)
	 */
	public void deleteUser(User inUser) throws UserManagerException {
		File file = loadUserFile(inUser.getUserName());
		file.delete();
		getUserNameToUserMap().remove(inUser.getUserName());
		fireUserEvent(inUser, "delete");
		// get rid of the old location too..

		Page item = getPageManager().getPage(
				"/WEB-INF/users/" + inUser.getId() + ".xml");
		getPageManager().removePage(item);
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

	/**
	 * Retrieve the map from group names to {@link FileSystemGroup}s.
	 *
	 * @return
	 */
	protected Map getGroupIdToGroupMap() {
		if (fieldGroupIdToGroupMap == null) {
			fieldGroupIdToGroupMap = new HashMap();
		}

		return fieldGroupIdToGroupMap;
	}

	/**
	 * Returns the map from usernames to {@link FileSystemUser}s.
	 *
	 * @return Map
	 */
	protected Map getUserNameToUserMap() {
		if (fieldUserNameToUserMap == null) {
			fieldUserNameToUserMap = new HashMap();
		}

		return fieldUserNameToUserMap;
	}

	public void flush() {
		getUserNameToUserMap().clear();
	}

	public Collection listGroupIds() {
		List ids = new ArrayList();
		ContentItem item = getPageManager().getRepository().get(
				getGroupDirectory());
		File groups = new File(item.getAbsolutePath());

		File[] groupfiles = groups.listFiles(new FilenameFilter() {
			public boolean accept(File inDir, String inName) {
				return inName.endsWith(".xml");
			}
		});

		if (groupfiles != null) {
			for (int i = 0; i < groupfiles.length; i++) {
				// Group group = new FileSystemGroup(this, groupfiles[i]);
				// this configures the users
				// getGroupNameToGroupMap().put(group.getName(), group);
				String name = PathUtilities.extractPageName(groupfiles[i]
						.getName());
				ids.add(name);
			}

		}

//		// Temporary
//		item = getPageManager().getRepository().get("/WEB-INF/groups");
//		groups = new File(item.getAbsolutePath());
//
//		groupfiles = groups.listFiles(new FilenameFilter() {
//			public boolean accept(File inDir, String inName) {
//				return inName.endsWith(".xml");
//			}
//		});
//
//		if (groupfiles != null) {
//			for (int i = 0; i < groupfiles.length; i++) {
//				String name = PathUtilities.extractPageName(groupfiles[i]
//						.getName());
//				if (!ids.contains(name)) {
//					ids.add(name);
//				}
//			}
//		}

		Collections.sort(ids);
		return ids;
	}

	/**
	 * Create a file representing the given group on disk.
	 *
	 * @param inGroupName
	 *            The name of the new group
	 *
	 * @return The new file
	 *
	 * @throws UserManagerException
	 *             If the new file could not be created
	 */
	protected File createGroupFile(String inGroupId)
			throws UserManagerException {
		try {
			File newFile = new File(getGroupDirectory(), inGroupId + ".xml");
			FileWriter writer = new FileWriter(newFile);

			try {
				writer.write("<?xml version=\"1.0\"?>\n" + "<group id=\""
						+ inGroupId + "\">\n" + "\t<group-name>" + inGroupId
						+ "</group-name>\n" + "<permissions/>\n" + "</group>\n");
			} finally {
				writer.close();
			}

			return newFile;
		} catch (Exception ex) {
			throw new UserManagerException(ex);
		}
	}

	/**
	 * Create a file representing the given user on disk.
	 *
	 * @param inUserName
	 *            The username of the new user
	 * @param inPassword
	 *            The password of the new user
	 *
	 * @return The new file
	 *
	 * @throws UserManagerException
	 *             If the new file could not be created
	 */
	protected File createUserFile(String inUserName, String inPassword)
			throws UserManagerException {
		try {
			File newFile = loadUserFile(inUserName);
			newFile.getParentFile().mkdirs();

			FileWriter writer = new FileWriter(newFile);

			try {
				writer.write("<?xml version=\"1.0\"?>\n" + "<user>\n"
						+ "  <user-name>" + inUserName + "</user-name>\n"
						+ "  <password>" + inPassword + "</password>\n"
						+ "<creation-date>" + new Date().getTime()
						+ "</creation-date>\n" + "</user>\n");
			} finally {
				writer.close();
			}

			return newFile;
		} catch (Exception ex) {
			throw new UserManagerException(ex);
		}
	}

	protected File loadGroupFile(String inGroupId) {
		ContentItem stub = getPageManager().getRepository().getStub(
				getGroupDirectory() + "/" + inGroupId + ".xml");
		File file = new File(stub.getAbsolutePath());
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	protected File loadUserFile(String inUserName) {
		ContentItem stub = getPageManager().getRepository().getStub(
				getUserDirectory() + "/" + inUserName + ".xml");
		File file = new File(stub.getAbsolutePath());
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.users.UserManager#getUserByEmail(java.lang.String)
	 */
	public User getUserByEmail(String emailaddress) throws UserManagerException {
		// check the cache first
		for (Iterator iter = getUserNameToUserMap().values().iterator(); iter
				.hasNext();) {
			User element = (User) iter.next();
			String email = element.getEmail();
			if (email != null && email.equalsIgnoreCase(emailaddress)) {
				return element;
			}
		}
		// TODO: replace with UserSearch object that can be replaced with Lucene
		// or JDBC
		for (Iterator iter = listUserNames().iterator(); iter.hasNext();) {
			String username = (String) iter.next();
			User element = getUser(username);// require null pointer check here
			if (element != null) {
				String email = element.getEmail();
				if (email != null && email.equalsIgnoreCase(emailaddress)) {
					return element;
				}
			}
		}
		return null;
	}

	public boolean isAllUsersLoaded() {
		return fieldAllUsersLoaded;
	}

	public void setAllUsersLoaded(boolean inAllUsersLoaded) {
		fieldAllUsersLoaded = inAllUsersLoaded;
	}

	public void saveUser(User user) throws UserManagerException {
		if (user.isVirtual()) {
			log.error("Cannot save virtual users: " + user.getUserName());
			return;
		}
		DocumentFactory factory = DocumentFactory.getInstance();
		Document doc = factory.createDocument();
		Element userElem = doc.addElement("user");
		userElem.addAttribute("enabled", Boolean.toString(user.isEnabled()));
		if (user.getUserName() == null) {
			int id = getUserIdCounter().incrementCount();
			String newid = String.valueOf(id);
			user.setId(newid);
		}
		Element userNameElem = userElem.addElement("user-name");
		userNameElem.addCDATA(user.getUserName());

		Element passwordElem = userElem.addElement("password");
		//
		if (user.getPassword() != null && !user.getPassword().equals("")) {
			String ps = user.getPassword();
			ps = encrypt(ps);
			// password may have changed we should set it so it's not in plain
			// text anymore.
			user.setPassword(ps);
			passwordElem.addCDATA(ps);
		}
		Element creationDateElem = userElem.addElement("creation-date");
		if (user.getCreationDate() != null) {
			creationDateElem.setText(String.valueOf(user.getCreationDate()
					.getTime()));
		} else {
			creationDateElem
					.setText(String.valueOf(System.currentTimeMillis()));
		}

		// Tuan add property lastLogined-Time
		Element lastLoginTime = userElem.addElement("lastLogined-Time");
		lastLoginTime.setText(DateStorageUtil.getStorageUtil()
				.formatForStorage(new Date()));

		MapPropertyContainer map = (MapPropertyContainer) user
				.getPropertyContainer();
		if (map != null) {
			Element propertiesElem = map.createPropertiesElement("properties");
			userElem.add(propertiesElem);
		}
		if (user.getGroups() != null) {
			for (Iterator iter = user.getGroups().iterator(); iter.hasNext();) {
				Group group = (Group) iter.next();
				Element child = userElem.addElement("group");
				child.addAttribute("id", group.getId());
			}
		}

		// File file = loadUserFile(user.getUserName());
		XmlFile xfile = new XmlFile();
		xfile.setRoot(doc.getRootElement());
		xfile.setPath(getUserDirectory() + "/" + user.getUserName() + ".xml");
		getXmlArchive().saveXml(xfile, null);

		getUserNameToUserMap().put(user.getUserName(), user);
	}

	public XmlArchive getXmlArchive() {
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive) {
		fieldXmlArchive = inXmlArchive;
	}

	public void saveGroup(Group inGroup) throws UserManagerException {
		Document doc = DocumentFactory.getInstance().createDocument();
		Element root = doc.addElement("group");
		if (inGroup.getId() == null) {

			int id = getUserIdCounter().incrementCount();
			String inAccount = String.valueOf(id);
			inGroup.setId(inAccount);

		}
		root.addAttribute("id", inGroup.getId());
		Element groupNameElem = root.addElement("group-name");
		groupNameElem.setText(inGroup.getName());

		Element permissionsElem = root.addElement("permissions");

		for (Iterator iter = inGroup.getPermissions().iterator(); iter
				.hasNext();) {
			Object permission = (Object) iter.next();
			Element permissionElem = permissionsElem.addElement("permission");
			permissionElem.setText(permission.toString());
		}

		MapPropertyContainer map = (MapPropertyContainer) inGroup
				.getPropertyContainer();
		if (map != null) {
			Element propertiesElem = map.createPropertiesElement("properties");
			root.add(propertiesElem);
		}
		File file = loadGroupFile(inGroup.getId());
		getXmlUtil().saveXml(doc, file);

		getGroupIdToGroupMap().remove(inGroup.getId());
	}

	public String encrypt(String inPassword) throws UserManagerException {
		try {
			if (inPassword.startsWith("DES:")) {
				return inPassword;
			}
			// long encryptionKey = 7939805759879765L; encryptionKey++;
			// StringEncryption encrypter = new StringEncryption(
			// StringEncryption.DES_ENCRYPTION_SCHEME, encryptionKey + "42" +
			// encryptionKey );
			String encryptedString = getStringEncryption().encrypt(inPassword);
			return encryptedString;
		} catch (OpenEditException ex) {
			throw new UserManagerException(ex);
		}
	}

	public IntCounter getUserIdCounter() {
		if (fieldUserIdCounter == null) {
			fieldUserIdCounter = new IntCounter();

			ContentItem item = getPageManager().getRepository().get(
					getUserDirectory() + "/users.properties");
			File users = new File(item.getAbsolutePath());

			fieldUserIdCounter.setCounterFile(users);
		}
		return fieldUserIdCounter;
	}

	public HitTracker getUsersInGroup(String inGroupId) {
		Group group = getGroup(inGroupId);
		if (group == null) {
			return null;
		}
		return getUsersInGroup(group);
	}

	// TODO: Replace with smart UserHitTracker that lazy loads
	public HitTracker getUsersInGroup(Group inGroup) {
		List all = new ArrayList();
		for (Iterator iter = getUsers().getAllHits(); iter.hasNext();) {

			User user = (User) iter.next();
			if (user.isInGroup(inGroup)) {
				all.add(user);
			}
		}
		Collections.sort(all, new UserComparator());
		HitTracker tracker = new ListHitTracker(all);
		return tracker;
	}

	public Authenticator getAuthenticator() {
		return fieldAuthenticator;
	}

	public void setAuthenticator(Authenticator inAuthenticator) {
		fieldAuthenticator = inAuthenticator;
	}

	protected void fireUserEvent(User inUser, String inOperation) {
		if (fieldWebEventHandler != null) {
			WebEvent event = new WebEvent();
			event.setOperation("authentication");
			event.setSearchType("user");
			event.setSource(this);
			event.addDetail("details", inOperation);
			event.setCatalogId(getCatalogId());
			event.setUser(inUser);
			getWebEventHandler().eventFired(event);
		}
	}

	public void logout(User inUser) {
		fireUserEvent(inUser, "logout");
	}

	protected WebEventHandler getWebEventHandler() {
		return fieldWebEventHandler;
	}

	public void setWebEventHandler(WebEventHandler inListener) {
		fieldWebEventHandler = inListener;
	}

	public PermissionsManager getPermissionsManager() {
		if (fieldPermissionsManager == null) {
			fieldPermissionsManager = new PermissionsManager();
			fieldPermissionsManager.setPageManager(getPageManager());
			fieldPermissionsManager.loadPermissions();
		}
		return fieldPermissionsManager;
	}

	public void setPermissionsManager(PermissionsManager inPermissionsManager) {
		fieldPermissionsManager = inPermissionsManager;
	}

	public XmlUtil getXmlUtil() {
		if (fieldXmlUtil == null) {
			fieldXmlUtil = new XmlUtil();
		}
		return fieldXmlUtil;
	}

	public void setXmlUtil(XmlUtil inXmlUtil) {
		fieldXmlUtil = inXmlUtil;
	}

	public User createGuestUser(String inAccount, String inPassword,
			String inGroupId) {

		if (inAccount == null) // when null then we are picking up the next user
								// from the list. May be dup username
		{
			int id = getUserIdCounter().incrementCount();
			inAccount = String.valueOf(id);
			// User user = getUser(inAccount);

		}

		FileSystemUser user = new FileSystemUser();
		user.setCreationDate(new Date());
		user.setUserName(inAccount);
		user.setPassword(inPassword);
		user.setVirtual(true);

		Group group = getGroup(inGroupId);
		if (group == null) {
			log.error("No such auto login group " + inGroupId);
		} else {
			user.addGroup(group);
		}
		return user;
	}

	public StringEncryption getStringEncryption() {
		return fieldStringEncryption;
	}

	public void setStringEncryption(StringEncryption inStringEncryption) {
		fieldStringEncryption = inStringEncryption;
	}

	public String decryptPassword(User inUser) throws OpenEditException {
		String pw = inUser.getPassword();
		if (pw.startsWith("DES:")) {
			pw = getStringEncryption().decrypt(pw);
		}
		return pw;
	}

	public String encryptPassword(User inUser) throws OpenEditException {
		String pw = inUser.getPassword();
		if (!pw.startsWith("DES:")) {
			pw = getStringEncryption().encrypt(pw);
		}
		return pw;
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

	public String nextId() {
		int id = getUserIdCounter().incrementCount();
		return String.valueOf(id);
	}
}
