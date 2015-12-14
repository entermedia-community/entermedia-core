/*
 * Created on Oct 19, 2004
 */
package org.openedit.users.filesystem;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.data.BaseSearcher;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.users.BaseUser;
import org.openedit.users.Group;
import org.openedit.users.User;
import org.openedit.users.UserSearcher;

/**
 *
 */
public class FileUserSearcher extends BaseSearcher implements UserSearcher
{
	private static final Log log = LogFactory.getLog(FileUserSearcher.class);
	protected XmlUserArchive fieldXmlUserArchive;

	@Override
	public Data createNewData()
	{
		BaseUser user = new BaseUser();
		return user;
	}

	public XmlUserArchive getXmlUserArchive()
	{
		if (fieldXmlUserArchive == null)
		{
			fieldXmlUserArchive = (XmlUserArchive) getModuleManager().getBean(getCatalogId(), "xmlUserArchive");
		}
		return fieldXmlUserArchive;
	}

	@Override
	public void reIndexAll() throws OpenEditException
	{

	}

	@Override
	public SearchQuery createSearchQuery()
	{

		return null;
	}

	@Override
	public HitTracker search(SearchQuery inQuery)
	{

		return null;
	}

	@Override
	public String getIndexId()
	{

		return null;
	}

	@Override
	public void clearIndex()
	{


	}

	@Override
	public void deleteAll(User inUser)
	{


	}

	@Override
	public void delete(Data inData, User inUser)
	{
		getXmlUserArchive().deleteUser((User)inData);
	}

	@Override
	public void saveAllData(Collection<Data> inAll, User inUser)
	{
		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			User user = (User) iterator.next();
			getXmlUserArchive().saveUser(user);			
		}

	}

	@Override
	public User getUser(String inAccount)
	{
		return getXmlUserArchive().getUser(inAccount);
	}

	@Override
	public User getUserByEmail(String inEmail)
	{

		return null;
	}

	@Override
	public HitTracker getUsersInGroup(Group inGroup)
	{

		return null;
	}

	@Override
	public void saveUsers(List inUserstosave, User inUser)
	{

		for (Iterator iterator = inUserstosave.iterator(); iterator.hasNext();)
		{
			User user = (User) iterator.next();
			getXmlUserArchive().saveUser(user);
		}

	}

}
