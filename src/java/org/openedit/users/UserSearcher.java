package org.openedit.users;

import java.util.List;

import org.openedit.data.Searcher;
import org.openedit.hittracker.HitTracker;
import org.openedit.util.StringEncryption;

public interface UserSearcher extends Searcher
{
	public abstract User getUser(String inAccount, boolean cached);

	public abstract User getUser(String inAccount);

	public abstract User getUserByEmail(String inEmail);

	public abstract HitTracker getUsersInGroup(Group inGroup);

	public abstract void saveUsers(List userstosave, User user);

	//public XmlUserArchive getXmlUserArchive();  //TODO: Remove this one day?
	StringEncryption getStringEncryption();
	public String encryptPassword(User inUser);
	String decryptPassword(User inUser);
	
}