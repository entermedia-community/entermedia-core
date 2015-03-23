package com.openedit.users;

import java.util.List;

import org.openedit.data.Searcher;

import com.openedit.hittracker.HitTracker;
import com.openedit.users.filesystem.XmlUserArchive;

public interface UserSearcher extends Searcher
{
	public abstract User getUser(String inAccount);

	public abstract User getUserByEmail(String inEmail);

	public abstract HitTracker getUsersInGroup(Group inGroup);

	public abstract void saveUsers(List userstosave, User user);

	public XmlUserArchive getXmlUserArchive();  //TODO: Remove this one day?
}