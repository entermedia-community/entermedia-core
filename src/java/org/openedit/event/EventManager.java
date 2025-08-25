package org.openedit.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.users.User;

import groovy.json.JsonOutput;

public class EventManager
{
	protected Map fieldFilteredListeners;
	protected SearcherManager fieldSearcherManager;

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public void fireEvent(WebEvent inEvent)
	{
		String action = inEvent.getSearchType() + "/" + inEvent.getOperation();
 
		String catalogid = inEvent.getCatalogId();
		if( catalogid == null)
		{
			throw new OpenEditException("Catalog Id required");
		}
			for (Iterator iterator = getFilteredListeners(catalogid).iterator(); iterator.hasNext();)
			{
				ListenerFilter listener = (ListenerFilter) iterator.next();
				if (inEvent.isCancelEvent())
				{
					break;
				}
				if( listener.shouldListen(action) )
				{
					listener.getListener().eventFired(inEvent);
				}
			}
	}

	public void addWebEventListener(String inCatalogId, WebEventListener inList)
	{
		getFilteredListeners(inCatalogId).add(inList);
	}
	public void removeWebEventListener(String inCatalogId, WebEventListener inList)
	{
		getFilteredListeners(inCatalogId).remove(inList);
	}
	
	public Collection getFilteredListeners(String inCatalogId)
	{
		if (fieldFilteredListeners == null)
		{
			fieldFilteredListeners = new HashMap();
		}
		Collection list = (Collection)fieldFilteredListeners.get(inCatalogId);
		if( list == null)
		{
			list = new ArrayList();
			//Look in the database
			Collection listeners = getSearcherManager().getList(inCatalogId, "webeventlistener");
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();)
			{
				Data data = (Data) iterator.next();
				String bean = data.get("beanname"); 
				WebEventListener listener = (WebEventListener)getSearcherManager().getModuleManager().getBean(inCatalogId, bean);
				
				Collection actionlist = (Collection)getSearcherManager().query(inCatalogId, "webeventlistenerfilter").match("webeventlistener", data.getId()).search();

				ListenerFilter actionfilter = new ListenerFilter();
				actionfilter.setListener(listener);
				String[] actions = new String[actionlist.size()];
				int i = 0;
				for (Iterator iterator2 = actionlist.iterator(); iterator2.hasNext();)
				{
					Data filter = (Data) iterator2.next();
					actions[i++] = filter.get("action");
				}
				actionfilter.setActions(actions);
				list.add(actionfilter);
			}
			fieldFilteredListeners.put(inCatalogId,list);
		}
		return list;
	}

	public void eventFired(WebEvent inEvent)
	{
		fireEvent(inEvent);//legacy
	}

	public void fireDataEditEvent(WebPageRequest inReq, Searcher inDataSearcher, Data inData)
	{
		Map changes= readChanges(inReq, inDataSearcher, inData);
		String viewid = inReq.findValue("viewid");
		fireDataEditEvent(inDataSearcher,inReq.getUser(),viewid,inData, changes);
	}
	public void fireDataEditEvent(Searcher inDataSearcher, User inUser, String inViewId, Data inData, Map inChanges)
	{
		if (!inChanges.isEmpty())
		{
			
			String logtype = inDataSearcher.getSearchType() + "editLog";
			
			WebEvent event = new WebEvent();
			event.setCatalogId(inDataSearcher.getCatalogId());
			event.setSearchType(inDataSearcher.getSearchType());
			event.setSource(this);
			event.setOperation("edit");
			event.setSourcePath(inData.getSourcePath());
			event.setProperty("id", inData.getId());
			event.setProperty(inDataSearcher.getSearchType() + "id", inData.getId());  //Deprecated
			event.setProperty("dataid", inData.getId());
			event.setProperty("sourcepath", inData.getSourcePath());
			
			//aka "changes"
			event.setProperty("viewid", inViewId);
			String changesstring = makeHumanLog(inDataSearcher,inData,inChanges);
			
			if (inViewId != null)
			{
				Data view = getSearcherManager().getCachedData(inDataSearcher.getCatalogId(), "view", inViewId);
				if (view != null)
				{
					changesstring = view.getName() + " \n" + changesstring;
				}
			}
			event.setProperty("details", changesstring);
			
			if (inDataSearcher.getSearchType().equals("asset"))
			{
				String jsonsource = makeJsonLog(inChanges);
				event.setValue("detailsjson", jsonsource);
			}
			event.setUser(inUser);
			fireEvent(event);
		}
	}
	
	/*

		String changes = readChanges(inReq, inSearcher, object);

		if (changes.length() > 0)
		{
			inReq.putPageValue("datachanges", changes);
			
			WebEvent event = new WebEvent();
			event.setCatalogId(inSearcher.getCatalogId());
			String type = inSearcher.getSearchType();
			event.setSearchType(type);
			event.setSource(this);
			event.setOperation("edit");
			event.setSourcePath(object.getSourcePath());
			event.setProperty("id", object.getId());
			event.setProperty(inSearcher.getSearchType() + "id", object.getId());

			event.setProperty("sourcepath", object.getSourcePath());
			
			//aka "changes"
			String changesstring = changes.toString();
			
			String viewid = inReq.findValue("viewid");
			event.setProperty("viewid", viewid);
			
			if (viewid != null)
			{
				Data view = getSearcherManager().getCachedData(inSearcher.getCatalogId(), "view", viewid);
				changesstring = view.getName() + " \n" + changesstring;
			}
			event.setProperty("details", changesstring);
			
			if (inSearcher.getSearchType().equals("asseteditLog"))
			{
				String jsonsource = readJSON(inReq, inSearcher, object);
				event.setValue("detailsjson", jsonsource);
			}
			event.setUser(inReq.getUser());
			fireEvent(event);
		}
	}
	*/
	
	public String makeJsonLog(Map changes)
	{
//		Data newdata = inDataSearcher.createNewData();
//		Map data = inData.getProperties();
//		newdata.getProperties().putAll(data);

		JSONObject json = new JSONObject();
		for(Iterator iterator = changes.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Object value = changes.get(key);
			if (value == null)
			{
				continue;
			}
			if (value instanceof Collection)
			{
				JSONArray jsonarray = new JSONArray();
				jsonarray.addAll((Collection) value);
				json.put(key, jsonarray);
			}
			else if (value instanceof Map)
			{
				JSONObject jsonmap = new JSONObject((Map) value);
				json.put(key, jsonmap);
			}
			else
			{
				json.put(key, value);
			}
		}
		
		String out = json.toJSONString();
		return out;
		
	}
	public Map readChanges(WebPageRequest inReq, Searcher inDataSearcher, Data inData) 
	{
		String[] fields = inReq.getRequestParameters("field");
		Data newdata = inDataSearcher.createNewData();
		inDataSearcher.updateData(inReq,fields,newdata );
		Map changemap = newdata.getProperties();
		return changemap;
	}
		
	public String makeHumanLog(Searcher inDataSearcher, Data inData, Map changes) 
	{
		StringBuffer textoutput = new StringBuffer();
		
		Collection fields = changes.keySet();
		for (Iterator iterator = fields.iterator(); iterator.hasNext();)
		{
			String field = (String) iterator.next();
			Object newvalue = changes.get(field);

			Object oldval = inData.getValue(field);

			if (newvalue == null && oldval == null)
			{
				continue;
			}
			if ((oldval == null || oldval.equals("")) && newvalue instanceof LanguageMap)
			{
				if (((LanguageMap) newvalue).isEmpty())
				{
					continue;
				}
			}
			if (newvalue != null && oldval == null)
			{
				if(newvalue.toString().isEmpty())
				{
					continue;
				}
			}

			PropertyDetail detail = inDataSearcher.getDetail(field);
			if (detail != null && detail.isList())
			{
				if (newvalue instanceof String && oldval instanceof String)
				{
					Data data = (Data)getSearcherManager().getCachedData(detail.getListCatalogId(), detail.getListId(), (String) oldval);
					if (data != null)
					{
						oldval = data.getName();
					}
					Data newdata = (Data)getSearcherManager().getCachedData(detail.getListCatalogId(), detail.getListId(), (String) newvalue);
					if (newdata != null)
					{
						newvalue = newdata.getName();
					}
				}
			}
			else {
				if (newvalue != null && newvalue.equals(oldval)) {
					continue;
				}

			}
			if (oldval == null)
			{
				oldval = "Empty";
			}
			textoutput.append(detail.getName() + ": " + oldval + " -> " + newvalue + "\n ");
			//textoutput.append();
		}
		return textoutput.toString();
	}

	public void fireDataSavedEvent(WebPageRequest inReq, Searcher inSearcher, Data data)
	{
		WebEvent event = new WebEvent();
		event.setSearchType(inSearcher.getSearchType());
		event.setCatalogId(inSearcher.getCatalogId());
		event.setOperation("saved");
		event.setProperty("dataid", data.getId());
		event.setProperty("id", data.getId());
		event.setProperty("note", "old field diff");
		event.setProperty("applicationid", inReq.findValue("applicationid"));
		event.setUser(inReq.getUser());
		event.setValue("data", data);
		fireEvent(event);
	}
	
	

	
}
