package org.openedit.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.WebPageRequest;
import org.openedit.hittracker.HitTracker;

public interface CompositeData extends Data
{
	
	public String get(String inId);


	public String getName();	
	public void setName(String inName);


	public void setProperty(String inId, String inValue);

	public Map getProperties();
	public Iterator<Data> iterator();
	public int size();
	public void refresh();

	public void setEditFields(Collection inFields);

	public void saveChanges(WebPageRequest inReq);
	
	public HitTracker getSelectedResults();
	
}
