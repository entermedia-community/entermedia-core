/*
 * Created on Sep 13, 2005
 */
package org.openedit.data;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.xml.XmlFile;

public class PropertyDetails extends AbstractCollection 
{
	protected List fieldDetails;
	protected Map fieldDetailsCached;
	
	protected Map fieldExternalIdCache;
	protected Map fieldDefaults;
	protected XmlFile fieldInputFile;
	protected String fieldPrefix;
	protected String fieldBeanName;
	
	public String getBeanName()
	{
		if( fieldBeanName == null)
		{
			if( getInputFile() != null && getInputFile().getRoot() != null)
			{
				fieldBeanName = getInputFile().getRoot().attributeValue("beanname");
			}
			if( fieldBeanName == null )
			{
				fieldBeanName = "dynamicSearcher";
			}
		}
		return fieldBeanName;
	}
	public void setBeanName(String inBeanName)
	{
		fieldBeanName = inBeanName;
	}
	public PropertyDetails()
	{
		// TODO Auto-generated constructor stub
	}
	public XmlFile getInputFile()
	{
		return fieldInputFile;
	}

	public void setInputFile(XmlFile inInputFile)
	{
		fieldInputFile = inInputFile;
	}

	public Map getDefaults() {
		if (fieldDefaults == null) {
			fieldDefaults = new HashMap();

		}

		return fieldDefaults;
	}

	public void setDefaults(Map inDefaults) {
		fieldDefaults = inDefaults;
	}

	long fieldLastLoaded;

	public List getDetails() {
		if (fieldDetails == null) {
			fieldDetails = new ArrayList();
		}
		return fieldDetails;
	}

	public void addDetail(PropertyDetail inDetail) {
		PropertyDetail oldDetail = getDetail(inDetail.getId());
		if(oldDetail != null){
			getDetails().remove(oldDetail);
		}
			
			getDetails().add(inDetail);
		
	}

	public List findIndexProperties() {
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isIndex()) {
				list.add(d);
			}
		}
		return list;
	}

	public List findKeywordProperties() {
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isKeyword()) {
				list.add(d);
			}
		}
		return list;
	}

	public List findStoredProperties() {
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isStored()) {
				list.add(d);
			}
		}
		return list;
	}

	// public List findSearchProperties()
	// {
	// List list = new ArrayList(getDetails().size());
	//		
	// for (Iterator iter = getDetails().iterator(); iter.hasNext();)
	// {
	// Detail d = (Detail) iter.next();
	//			
	// if ( d.isSearchable() )
	// {
	// list.add(d);
	// }
	// }
	// return list;
	// }
	//	

	public boolean contains(String inKey) {
		PropertyDetail det = getDetail(inKey);

		return det != null;
	}
	
	public Map<String,PropertyDetail> getDetailsCached()
	{
		if (fieldDetailsCached == null)
		{
			fieldDetailsCached = new HashMap(getDetails().size());
			for (Iterator iter = getDetails().iterator(); iter.hasNext();) 
			{
				PropertyDetail detail = (PropertyDetail) iter.next();
				fieldDetailsCached.put(detail.getId(),detail);
			}
		}
		return fieldDetailsCached;
	}

	public PropertyDetail getDetail(String inId) {
		if (inId == null) {
			return null;
		}
		PropertyDetail detail = getDetailsCached().get(inId);
		
		return detail;
	}

	public void removeDetail(String inId) {
		PropertyDetail toRemove = null;
		for (Iterator iterator = getDetails().iterator(); iterator.hasNext();) {
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if (inId.equals(detail.getId())) {
				toRemove = detail;
			}
		}
		if (toRemove != null) {
			getDetails().remove(toRemove);
		}
	}

	public PropertyDetail getDetailByExternalId(String inName) {
		PropertyDetail found = (PropertyDetail) getExternalIdCache()
				.get(inName);
		if (found == null) {
			for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
				PropertyDetail detail = (PropertyDetail) iter.next();
				String[] all = detail.getExternalIds();

				if (all != null) {
					for (int i = 0; i < all.length; i++) 
					{
						String id = all[i];
						//strip off the : from XMP-dc:Title
						int index = id.indexOf(':');
						if( index > 0 )
						{
							id = id.substring(index + 1);
						}
						if (inName.equals(id) || inName.equals(id.replace(" ", ""))) 
						{
							found = detail;
							getExternalIdCache().put(inName, found);
							break;
						}
					}
				}
			}
		}
		return found;
	}

	public List getDetailsByProperty(String property, String value) {
		List properties = new ArrayList();
		for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
			PropertyDetail detail = (PropertyDetail) iter.next();
			String val = detail.get(property);
			if (value.equals(val)) {
				properties.add(detail);
			}
		}
		return properties;
	}

	public List getDetailsByType(String property, String value) {
		List properties = new ArrayList();
		for (Iterator iter = getDetails().iterator(); iter.hasNext();) {
			PropertyDetail detail = (PropertyDetail) iter.next();
			String val = detail.get(property);
			if (value.equals(val)) {
				properties.add(detail);
			}
		}
		return properties;
	}

	public long getLastLoaded() {
		return fieldLastLoaded;
	}

	public void setLastLoaded(long inLastLoaded) {
		fieldLastLoaded = inLastLoaded;
	}

	public Iterator iterator() {
		return getDetails().iterator();
	}

	public int size() {
		return getDetails().size();
	}

	public void setDetails(List inNewdetails) {
		fieldDetails = new ArrayList(inNewdetails);
	}

	public Map getExternalIdCache() {
		if (fieldExternalIdCache == null) {
			fieldExternalIdCache = new HashMap();
		}

		return fieldExternalIdCache;
	}
	public String getPrefix() 
	{
		if( fieldPrefix == null)
		{
			if( getInputFile() != null && getInputFile().getRoot() != null)
			{
				fieldPrefix = getInputFile().getRoot().attributeValue("prefix");
			}
		}
		return fieldPrefix;
	}
	public void setPrefix(String fieldPrefix) 
	{
		this.fieldPrefix = fieldPrefix;
	}

}
