package org.openedit.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.data.SearcherManager;

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
				
				Collection actionlist = (Collection)getSearcherManager().query(inCatalogId, "webeventlistenerfilter").match("webeventlistener", data.getId());

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

	
}
