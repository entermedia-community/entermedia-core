package com.openedit.users;

import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.xml.ElementData;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.hittracker.HitTracker;

public class UserPreferences extends ElementData
{ 
	protected Map fieldResultViews;
	protected XmlFile fieldUserData;
	protected XmlArchive fieldXmlArchive;
	protected User fieldUser;
	protected HitTracker fieldCatalogs;
	protected HitTracker fieldUploadCatalogs;
	
	public HitTracker getCatalogs()
	{
		return fieldCatalogs;
	}

	public void setCatalogs(HitTracker inCatalogs)
	{
		fieldCatalogs = inCatalogs;
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public User getUser()
	{
		return fieldUser;
	}

	public void setUser(User inUser)
	{
		fieldUser = inUser;
	}

	public XmlFile getUserData()
	{
		return fieldUserData;
	}

	public void setUserData(XmlFile inUserData)
	{
		fieldUserData = inUserData;
		setElement(inUserData.getRoot());
	}

	public Map getResultViews()
	{
		return fieldResultViews;
	}

	public void setResultViews(Map inResultViews)
	{
		fieldResultViews = inResultViews;
	}
	//should not call this method
//	protected String getResultViewPreference(String inView)
//	{
//		Element id = getUserData().getElementById(inView);
//		if(id != null)
//		{
//			return(id.attributeValue("view"));
//			
//		}
//		return null;
//	}

	public void setResultViewPreference(String inView, String inPreference)
	{
		Element id = findSetting(inView);
		if( id == null)
		{
			id = getUserData().addNewElement();
		}
		id.addAttribute("id", inView);
		id.addAttribute("view", inPreference);
	}
	
	public int getHitsPerPageForSearchType(String inResultsView) throws Exception
	{
		//asset search
		Element id = findSetting(inResultsView);
		if( id == null)
		{
			//This view does not really exist. Send back default standard view..
			//TODO: Load up defaultresultview.xml
			if( "selection".equals(inResultsView ))
			{
				return getHitsPerPageForSearchType("album");
			}
		}
		else
		{
			String val = id.attributeValue("hitsperpage");
			if( val != null)
			{
				return Integer.parseInt( val );
			}
		}
		return 25;
	}
	public void setHitsPerPageForSearchType(String inResultsView, int inHits)
	{
		Element id = findSetting(inResultsView);
		if( id == null)
		{
			id = getUserData().createElement();
			id.addAttribute("id", inResultsView);
		}
		id.addAttribute("hitsperpage", String.valueOf(inHits));
		save();
	}
	public void setSortForSearchType(String inResultsView, String inSort) 
	{
		Element id = findSetting(inResultsView);
		if( id == null)
		{
			id = getUserData().createElement();
			id.addAttribute("id", inResultsView);
		}
		id.addAttribute("sort", inSort);
		save();
	}
	public String getSortForSearchType(String inResultsType)
	{
		//asset search
		Element id = findSetting(inResultsType);
		if( id == null)
		{
			//This view does not really exist. Send back default standard view..
			//TODO: Load up defaultresultview.xml
			if( inResultsType != null && !inResultsType.equals("search"))
			{
				return getSortForSearchType("search");
			}
		}
		if( id == null)
		{
			return null;
		}
		String sort =  id.attributeValue("sort");
		if( sort == null || sort.length() == 0)
		{
			return null;
		}
		return sort;
	}
	public String getViewForResultType(String inCustomView, String inResultsView)
	{
		if( inCustomView != null)
		{
			return inCustomView;
		}
		return getViewForResultType(inResultsView);
	}	
	public String getViewForResultType(String inResultsView)
	{
		//asset search
		String view = null;
		Element id = findSetting(inResultsView);
		if( id == null)
		{
			//This view does not really exist. Send back default standard view..
			//TODO: Load up defaultresultview.xml
//			if( "selection1".equals(inResultsView))
//			{
//				view = "thumbs";
//			}
//			else if( "selection2".equals(inResultsView))
//			{default
//				view = "categorize";
//			}
//			else if( "selection3".equals(inResultsView))
//			{
//				view = "download";
//			}
			if( inResultsView != null && inResultsView.startsWith("selection"))
			{
				return getViewForResultType("album");
			}
		}
		else
		{
			view = id.attributeValue("view");
		}
		if( view == null)
		{
			view = "thumbs";
		}
		return view;
			// String current = user.get("resulttype");
//			// legacy support for data-mining
//			if (use == null)
//			{
//				use = user.get("resulttype");
//				if ("table".equals(use))
//				{
//					user.setProperty("resulttype", "icon");
//				}
//				else
//				{
//					user.setProperty("resulttype", "table");
//				}
//				return;
//			}
			// end legacy support section

//			String catalogid = inReq.findValue("catalogid");
//			HitTracker lht = getSearcherManager().getList(catalogid, "resulttype");
//			String hitsperpage = null;
//			if (lht != null)
//			{
//				ElementData temp = (ElementData) lht.getById(use);
//				if (temp != null)
//				{
//					hitsperpage = temp.get("hitsperpage");
//				}
//			}
//			if (user != null)
//			{
//				user.setProperty("resulttype", use);
//				user.setProperty("hitsperpage", hitsperpage);
//				getUserManager().saveUser(user);
//			}
			//inReq.setRequestParameter("hitsperpage", hitsperpage);

//			HitTracker ht = loadHits(inReq);
//			if (ht == null)
//			{
//				return;
//			}
//			else
//			{
//				ht.setResultsView(use);
//				ht.setHitsPerPage(Integer.parseInt(hitsperpage));
//				inReq.putSessionValue(ht.getSessionId(), ht);
//			}
//		}
		
	}

	private Element findSetting(String inResultsView)
	{
		Element id = getUserData().getElementById(inResultsView);
		if( id == null)
		{
			id = getUserData().getElementById("asset");
		}
		return id;
	}

	public void save()
	{
		if( getUser() != null)
		{
			getXmlArchive().saveXml(getUserData(), getUser());
		}
	}
	public Data getLastCatalog()
	{
		String catid = get("lastcatalog");
		for (Iterator iterator = getCatalogs().iterator(); iterator.hasNext();)
		{
			Data cat = (Data) iterator.next();
			if( catid == null || cat.getId().equals(catid))
			{
				return cat;
			}
			
		}
		if( getCatalogs().size() > 0)
		{
			return (Data)getCatalogs().iterator().next();
		}
		return null;
	}

	public HitTracker getUploadCatalogs()
	{
		return fieldUploadCatalogs;
	}

	public void setUploadCatalogs(HitTracker inUploadCatalogs)
	{
		fieldUploadCatalogs = inUploadCatalogs;
	}

	public void removeSearchType(String inOldresulttype)
	{
		// TODO Auto-generated method stub
		Element id = getUserData().getElementById(inOldresulttype);
		if( id != null)
		{
			getUserData().deleteElement(id);
		}
	}
}
