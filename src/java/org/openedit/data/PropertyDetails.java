/*
 * Created on Sep 13, 2005
 */
package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.xml.XmlFile;

public class PropertyDetails extends ArrayList
{
	private static final Log log = LogFactory.getLog(PropertyDetails.class);

	protected String fieldId;

	protected List fieldDetails;
	protected Map fieldDetailsCached;
	protected Map fieldExternalIdCache;
	protected Map fieldDefaults;
	protected XmlFile fieldInputFile;
	protected String fieldPrefix;
	protected String fieldBeanName;
	protected String fieldToString;
	protected String fieldClassName;//The object type to create
	protected String fieldDependsOnText;
	protected boolean fieldAllowDynamicFields;;
	protected PropertyDetailsArchive fieldArchive;
	protected String fieldSearchTypes;//The tables to search for a multi-search

	protected XmlFile fieldBaseSettings;
	

	public XmlFile getBaseSettings()
	{
		return fieldBaseSettings;
	}

	public void setBaseSettings(XmlFile inBaseSettings)
	{
		fieldBaseSettings = inBaseSettings;
	}

	public PropertyDetailsArchive getArchive()
	{
		return fieldArchive;
	}

	public void setArchive(PropertyDetailsArchive inArchive)
	{
		fieldArchive = inArchive;
	}

	public String getId()
	{
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
	}
	
	public String getDependsOnText()
	{
		return fieldDependsOnText;
	}

	public void setDependsOnText(String inDependsOnText)
	{
		fieldDependsOnText = inDependsOnText;
	}

	public void setAllowDynamicFields(boolean inAllowDynamicFields)
	{
		if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			if(inAllowDynamicFields){
				getInputFile().getRoot().attributeValue("allowdynamicfields", "true");
			} else{
				getInputFile().getRoot().attributeValue("allowdynamicfields", "false");

			}		
			
		}
		
		
		fieldAllowDynamicFields = inAllowDynamicFields;
	}

	public boolean isAllowDynamicFields()
	{
		if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			String lazy = getInputFile().getRoot().attributeValue("allowdynamicfields");
			if (lazy != null)
			{
				return Boolean.parseBoolean(lazy);
			}
			
		}
		return false;
	}

	
	public PropertyDetails()
	{
		// TODO Auto-generated constructor stub
	}
	public PropertyDetails(PropertyDetailsArchive inArchive, String inId)
	{
		setArchive(inArchive);
		setId(inId);
	}
	public Boolean isLazyInit()
	{

		if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			String lazy = getInputFile().getRoot().attributeValue("lazy-init");
			if (lazy != null)
			{
				return Boolean.parseBoolean(lazy);
			}
			
		}
		return true;

	}

	public String getClassName()
	{
		if (fieldClassName == null)
		{
			if( fieldBaseSettings != null && getBaseSettings().getRoot() != null)
			{
				return getBaseSettings().getRoot().attributeValue("class");
			}
			else if (getInputFile() != null && getInputFile().getRoot() != null)
			{
				return getInputFile().getRoot().attributeValue("class");
			}
			//			if( fieldBeanName == null )
			//			{
			//				fieldBeanName = "dynamicSearcher";
			//			}
		}
		return fieldClassName;
	}

	public void setClassName(String inClassName)
	{
		fieldClassName = inClassName;
	}

	
	public String getSettingValue(String inKey) {
		if( fieldBaseSettings != null && getBaseSettings().getRoot() != null)
		{
			return getBaseSettings().getRoot().attributeValue(inKey);
		}
		else if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			return getInputFile().getRoot().attributeValue(inKey);
		}
		
		return null;
	}
	
	
	public String getBeanName()
	{
		if (fieldBeanName == null)
		{
			if (getInputFile() != null && getInputFile().getRoot() != null)
			{
				fieldBeanName =  getInputFile().getRoot().attributeValue("beanname");
			}
			if(fieldBeanName == null && fieldBaseSettings != null && getBaseSettings().getRoot() != null)
			{
				fieldBeanName = getBaseSettings().getRoot().attributeValue("beanname");
			}
			//			if( fieldBeanName == null )
			//			{
			//				fieldBeanName = "dynamicSearcher";
			//			}
		}
		return fieldBeanName;
	}

	public void setBeanName(String inBeanName)
	{
		fieldBeanName = inBeanName;
	}

	/**
	 * This is used for autocomplete fields when searching on this table that
	 * may not have a name column
	 * 
	 * @return
	 */
	public String getRender()
	{
		if (fieldToString == null)
		{
			if (getInputFile() != null && getInputFile().getRoot() != null)
			{
				fieldToString = getInputFile().getRoot().attributeValue("tostring");
			}
			//			if( fieldBeanName == null )
			//			{
			//				fieldBeanName = "dynamicSearcher";
			//			}
		}
		return fieldToString;
	}

	public void setRender(String inBeanName)
	{
		
		if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			 getInputFile().getRoot().setAttributeValue("tostring", inBeanName);
		}
		
		
		fieldToString = inBeanName;
	}

	

	public XmlFile getInputFile()
	{
		return fieldInputFile;
	}

	public void setInputFile(XmlFile inInputFile)
	{
		fieldInputFile = inInputFile;
	}

	public Map getDefaults()
	{
		if (fieldDefaults == null)
		{
			fieldDefaults = new HashMap();

		}

		return fieldDefaults;
	}

	public void setDefaults(Map inDefaults)
	{
		fieldDefaults = inDefaults;
	}

	long fieldLastLoaded;

	public List getDetails()
	{
		if (fieldDetails == null)
		{
			fieldDetails = new ArrayList();
		}
		return fieldDetails;
	}

	public void addDetail(PropertyDetail inDetail)
	{
		PropertyDetail oldDetail = getDetail(inDetail.getId());
		if (oldDetail != null)
		{
			getDetails().remove(oldDetail);
		}

		getDetails().add(inDetail);
		getDetailsCached().remove(inDetail.getId());
		getDetailsCached().put(inDetail.getId(), inDetail);
	}

	public List findIndexProperties()
	{
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isIndex()  && !d.isDeleted())
			{
				list.add(d);
			}
		}
		return list;
	}

	public List findKeywordProperties()
	{
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isKeyword() || d.getId() == "name")
			{
				list.add(d);
			}
		}
		return list;
	}

//	public List findStoredProperties()
//	{
//		List list = new ArrayList(getDetails().size());
//		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
//		{
//			PropertyDetail d = (PropertyDetail) iter.next();
//			if (d.isStored())
//			{
//				list.add(d);
//			}
//		}
//		return list;
//	}
	
	public List findBadgesProperties()
	{
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isBadge() )
			{
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

	public boolean contains(String inKey)
	{
		PropertyDetail det = getDetail(inKey);

		return det != null;
	}

	public Map<String, PropertyDetail> getDetailsCached()
	{
		if (fieldDetailsCached == null)
		{
			fieldDetailsCached = new HashMap(getDetails().size());
			for (Iterator iter = getDetails().iterator(); iter.hasNext();)
			{
				PropertyDetail detail = (PropertyDetail) iter.next();
				fieldDetailsCached.put(detail.getId(), detail);
			}
		}
		return fieldDetailsCached;
	}

	public PropertyDetail getDetail(String inId)
	{
		if (inId == null || inId.startsWith("."))
		{
			return null;
		}
		if( inId.contains("."))
		{
			String[] type = inId.split("\\.");
			if( type[1].length() == 2) //language.en
			{
				PropertyDetail localinfo = getDetail(type[0]);
				if(localinfo != null && !(localinfo.isDataType("nested")|| localinfo.isDataType("objectarray")) && localinfo.isMultiLanguage()){
						inId = type[0];
				}
			}
			else
			{
				//Remote lookup
				//By searchtype
				//See if there is one local
				PropertyDetail localinfo = getDetail(type[0]);
				if( localinfo == null)
				{
					PropertyDetails otherdetails = getArchive().getPropertyDetailsCached(type[0]);
					if( otherdetails != null)
					{
						PropertyDetail shareddetail = otherdetails.getDetail(type[1]);
						return shareddetail;
					}
				}
				if(localinfo != null)
				{
					if( localinfo.isList() )
					{
						PropertyDetails remotedetails = getArchive().getPropertyDetails(localinfo.getListId());
						PropertyDetail shareddetail = remotedetails.getDetail(type[1]);
						if(shareddetail != null){
							return shareddetail;
						}
					}
					if( localinfo.isDataType("nested")|| localinfo.isDataType("objectarray"))
					{
						localinfo = localinfo.getChildDetail(type[1]);
						if( localinfo != null)
						{
							PropertyDetail copy = localinfo.copy();
							copy.setId(inId);
							return copy;
						}
					}
				}
				//Object stuff
				log.info("Loading " + inId );
				
			}
		}

		PropertyDetail detail = getDetailsCached().get(inId);

		return detail;
	}

	public void removeDetail(String inId)
	{
		PropertyDetail toRemove = null;
		for (Iterator iterator = getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if (inId.equals(detail.getId()))
			{
				toRemove = detail;
			}
		}
		if (toRemove != null)
		{
			getDetails().remove(toRemove);
		}
	}

	/**
	 * inName should be lower case with or without spaces
	 * 
	 * @param inName
	 * @return
	 */
	public PropertyDetail getDetailByExternalId(String inName)
	{
		PropertyDetail found = (PropertyDetail) getExternalIdCache().get(inName);
		if (found == null)
		{
			for (Iterator iter = getDetails().iterator(); iter.hasNext();)
			{
				PropertyDetail detail = (PropertyDetail) iter.next();
				String[] all = detail.getExternalIds();

				if (all != null)
				{
					for (int i = 0; i < all.length; i++)
					{
						String id = all[i].toLowerCase();
						String targetname = inName.toLowerCase();
						//strip off the : from XMP-dc:Title
						int index = id.indexOf(':');
						if (index > 0)
						{
							id = id.substring(index + 1);
						}
						if (targetname.equals(id) || targetname.equals(id.replace(" ", "")))
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

	public List getDetailsByProperty(String property, String value)
	{
		//TODO: Add a cache here. 

		List properties = new ArrayList();
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iter.next();
			String val = detail.get(property);
			if (value.equals(val))
			{
				properties.add(detail);
			}
		}
		return properties;
	}
	

	public PropertyDetail getDetailByProperty(String property, String value)
	{
		//TODO: Add a cache here. 

		List properties = new ArrayList();
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iter.next();
			String val = detail.get(property);
			if (value.equals(val))
			{
				return detail;
			}
		}
		return null;
	}
	
	

	public List getDetailsByType(String property, String value)
	{
		List properties = new ArrayList();
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iter.next();
			String val = detail.get(property);
			if (value.equals(val))
			{
				properties.add(detail);
			}
		}
		return properties;
	}

	public long getLastLoaded()
	{
		return fieldLastLoaded;
	}

	public void setLastLoaded(long inLastLoaded)
	{
		fieldLastLoaded = inLastLoaded;
	}

	public Iterator iterator()
	{
		return getDetails().iterator();
	}

	public int size()
	{
		return getDetails().size();
	}

	public void setDetails(List inNewdetails)
	{
		fieldDetails = new ArrayList(inNewdetails);
	}

	public Map getExternalIdCache()
	{
		if (fieldExternalIdCache == null)
		{
			fieldExternalIdCache = new HashMap();
		}

		return fieldExternalIdCache;
	}

	public String getPrefix()
	{
		if (fieldPrefix == null)
		{
			if( fieldBaseSettings != null && getBaseSettings().getRoot() != null)
			{
				return getBaseSettings().getRoot().attributeValue("prefix");
			}
			else if (getInputFile() != null && getInputFile().getRoot() != null)
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

	public Collection findAutoIncludeProperties()
	{
		List list = new ArrayList(getDetails().size());
		for (Iterator iter = getDetails().iterator(); iter.hasNext();)
		{
			PropertyDetail d = (PropertyDetail) iter.next();
			if (d.isAutoInclude())
			{
				list.add(d);
			}
		}
		return list;
	}
	
	
	public int getMultilanguageFieldCount(){
		int count = 0;
		for (Iterator iterator = getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if(detail.isMultiLanguage()){
				count++;
			}
			
		}
		return count;	
		
	}
	
	
	public PropertyDetail findCurrentFromLegacy(String inId){
		for (Iterator iterator = getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if(inId == null){
				return null;
			}
			if(inId.equals(detail.get("legacy"))){
				return detail;
			}
		}
		return null;
	}

	public PropertyDetail getLegacyDetail(String inPropid) {
		for (Iterator iterator = getDetails().iterator(); iterator.hasNext();) {
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if(inPropid.equals(detail.get("legacy"))) {
				return detail;
			}
		}
		return null;
	}

	public String getSearchTypes()
	{
		if (fieldSearchTypes == null)
		{
			if( fieldBaseSettings != null && getBaseSettings().getRoot() != null)
			{
				return getBaseSettings().getRoot().attributeValue("searchtypes");
			}
			else if (getInputFile() != null && getInputFile().getRoot() != null)
			{
				return getInputFile().getRoot().attributeValue("searchtypes");
			}
			//			if( fieldBeanName == null )
			//			{
			//				fieldBeanName = "dynamicSearcher";
			//			}
		}
		return fieldSearchTypes;
	}

	
	public String getBaseSetting(String inSetting) {
		String val = null;
		if( fieldBaseSettings != null && getBaseSettings().getRoot() != null)
		{
			val = getBaseSettings().getRoot().attributeValue(inSetting);
		}
		else if (getInputFile() != null && getInputFile().getRoot() != null)
		{
			val = getInputFile().getRoot().attributeValue(inSetting);
		}
		return val;
		
	}

	public PropertyDetail getDetailByName(String inName)
	{
		PropertyDetail found = (PropertyDetail) getExternalIdCache().get(inName);
		if (found == null)
		{
			for (Iterator iter = getDetails().iterator(); iter.hasNext();)
			{
				PropertyDetail detail = (PropertyDetail) iter.next();
				if( detail.getName() != null && detail.getName().equals(inName))
				{
					found = detail;
					getExternalIdCache().put(inName, found);
					break;
				}
			}
		}
		return found;
	}

	public List findRequiredProperties() {
			List list = new ArrayList(getDetails().size());
			for (Iterator iter = getDetails().iterator(); iter.hasNext();)
			{
				PropertyDetail d = (PropertyDetail) iter.next();
				if (d.isRequired()  && !d.isDeleted())
				{
					list.add(d);
				}
			}
			return list;
	}
	
}
