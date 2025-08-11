package org.openedit.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.modules.translations.LanguageMap;

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

	public void fireDataEditEvent(WebPageRequest inReq, Searcher inSearcher, Data object)
	{

		String changes = readChanges(inReq, inSearcher, object);

		if (changes.length() > 0)
		{
			inReq.putPageValue("datachanges", changes);
			
			WebEvent event = new WebEvent();
			event.setCatalogId(inSearcher.getCatalogId());
			String type = inSearcher.getSearchType();
			event.setSearchType(type);
			event.setSource(this);
			//assetedit

			//			event.setSearchType("assetedit");
			//			event.setSource(this);
			//			event.setOperation("assetedit");
			//			event.setSourcePath(asset.getSourcePath());

			//			event.setProperty("assetid", asset.getId());
			//			event.setProperty("assetname", asset.getName());
			//			event.setProperty("changes", changes.toString());

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
			
			String jsonsource = readJSON(inReq, inSearcher, object);
			event.setValue("detailsjson", jsonsource);
			
			event.setUser(inReq.getUser());
			fireEvent(event);
		}
	}
	
	public String readJSON(WebPageRequest inReq, Searcher inSearcher, Data object)
	{
		Data compare = inSearcher.createNewData();
		String[] fields = inReq.getRequestParameters("field");
		if (fields == null)
		{
			return null;
		}
		
		inSearcher.updateData(inReq, fields, compare);
		
		String jsonsource = compare.toJsonString();
		return jsonsource;
	}

	public String readChanges(WebPageRequest inReq, Searcher inSearcher, Data object) {
		StringBuffer changes = new StringBuffer();
		String[] fields = inReq.getRequestParameters("field");
		if (fields == null)
		{
			return null;
		}
		//		if (composite != null)
		//		{
		//			changes.append("MutliEdit->");
		//		}
		Data compare = inSearcher.createNewData();
		inSearcher.updateData(inReq, fields, compare);
		for (int i = 0; i < fields.length; i++)
		{
			String field = fields[i];
			Object value = compare.getValue(field);

			Object oldval = object.getValue(field);

			if (value == null && oldval == null)
			{
				continue;
			}
			if ((oldval == null || oldval.equals("")) && value instanceof LanguageMap)
			{
				if (((LanguageMap) value).isEmpty())
				{
					continue;
				}
			}
			if (value != null && oldval == null)
			{
				if(value.toString().isEmpty())
				{
					continue;
				}
			}

			if (value != null && !value.equals(oldval))
			{
				PropertyDetail detail = inSearcher.getDetail(field);
				if (detail != null && detail.isList())
				{
					if (value instanceof String && oldval instanceof String)
					{
						Searcher listSearcher = getSearcherManager().getListSearcher(detail);
						Data data = (Data) listSearcher.searchById((String) oldval);
						if (data != null)
						{
							oldval = data.getName();
						}
						data = (Data) listSearcher.searchById((String) value);
						if (data != null)
						{
							value = data.getName();
						}
					}
				}
				if (changes.length() > 0)
				{
					changes.append(", ");
				}
				if (oldval == null)
				{
					oldval = "Empty";
				}
				changes.append(detail.getName() + ": " + oldval + " -> " + value + "\n");
			}
		}
		return changes.toString();
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
