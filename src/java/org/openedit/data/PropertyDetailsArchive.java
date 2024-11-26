package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.page.manage.TextLabelManager;
import org.openedit.profile.UserProfile;
import org.openedit.repository.ContentItem;
import org.openedit.users.User;
import org.openedit.util.PathUtilities;
import org.openedit.xml.ElementData;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

public class PropertyDetailsArchive implements CatalogEnabled
{
	private static final Log log = LogFactory.getLog(PropertyDetailsArchive.class);
	protected XmlArchive fieldXmlArchive;
	protected String fieldCatalogId;
	protected Map fieldViewLabels;
	protected TextLabelManager fieldTextLabelManager;
	protected PageManager fieldPageManager;
	protected SearcherManager fieldSearcherManager;
	protected Map<String, View> fieldViewCache;
	protected Map fieldPropertyDetails;
	protected List fieldChildTables;
	protected List fieldChildTableNames;
	protected String fieldSaveTo = "data"; //base,catalog,data
	protected List fieldSearchTypes;

	public String getSaveTo()
	{
		return fieldSaveTo;
	}

	public void setSaveTo(String inSaveTo)
	{
		fieldSaveTo = inSaveTo;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public TextLabelManager getTextLabelManager()
	{
		return fieldTextLabelManager;
	}

	public void setTextLabelManager(TextLabelManager inTextLabelManager)
	{
		fieldTextLabelManager = inTextLabelManager;
	}

	public List getDataProperties(String inType)
	{
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null)
		{
			return Collections.EMPTY_LIST;
		}
		return details.getDetails();
	}

	public List getIndexProperties(String inType)
	{
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null)
		{
			return Collections.EMPTY_LIST;
		}
		return details.findIndexProperties();
	}

//	public List getStoredProperties(String inType)
//	{
//		PropertyDetails details = getPropertyDetails(inType);
//		if (details == null)
//		{
//			return Collections.EMPTY_LIST;
//		}
//		return details.findStoredProperties();
//	}

	/**
	 * This returns a subset of the list of Detail's as found in
	 * store/configuration/{inType}.xml The subset is determined by
	 * inSubsetProperties.
	 * 
	 * @param inType
	 *            The type of data (i.e., product, catalog, item, order, etc...)
	 * @param inView
	 *            Identifies the file to look in for the subset of properties to
	 *            take from those found in inType. The file is
	 *            store/configuration/fields/properties{inSubsetProperties}.xml
	 * @param inUser
	 * @return A List of Details
	 * @deprecated
	 */
	public List getDataProperties(String inType, String inView, User inUser)
	{
		PropertyDetails propdetails = getPropertyDetailsCached(inType);
		if (propdetails == null)
		{
			log.error("No such properties file " + inType);
			return null;
		}
		return getDetails(propdetails, inView, inUser);
	}

	public List getDataProperties(String inType, String inView, UserProfile inProfile)
	{
		PropertyDetails propdetails = getPropertyDetailsCached(inType);
		if (propdetails == null)
		{
			log.error("No such properties file " + inType);
			return null;
		}
		return getDetails(propdetails, inView, inProfile);
	}

	public boolean viewExists(String inView)
	{
		XmlFile file = getViewXml(inView);
		return file.isExist();
	}

	protected XmlFile getViewXml(String inViewPath)
	{
		String savepath = findConfigurationFile("/views/" + inViewPath + ".xml");
		XmlFile file = getXmlArchive().getXml(savepath);
		
		if( !file.isExist() )
		{
			String name = "edit";  //general
			
			if( inViewPath.contains("results") || inViewPath.contains("filter"))
			{
				name = "results";
			}
			else if( inViewPath.contains("addnew") )
			{
				name = "addnew";
			} 
			String path = "/" + getCatalogId() + "/data/views/default/" + name + ".xml";
			file = getXmlArchive().getXml(path);
			//save here
			savepath = "/WEB-INF/data/" + getCatalogId() + "/views/" + inViewPath;
			file.setPath(savepath);
		}
		
		return file;
	}

	public View getView(String inSearchType, String inView, UserProfile inProfile)
	{
		PropertyDetails details = getPropertyDetailsCached(inSearchType);
		View view = getView(details, inView, inProfile);
		return view;
	}

	/**
	 * @deprecated use getView(
	 */
	public View getDetails(PropertyDetails propdetails, String inView, User inUser)
	{
		return getView(propdetails, inView, (UserProfile) null);
	}

	/**
	 * @deprecated use getView(
	 */
	public View getDetails(PropertyDetails propdetails, String inView, UserProfile inProfile)
	{
		return getView(propdetails, inView, inProfile);
	}

	public View getView(PropertyDetails propdetails, String inView, UserProfile inProfile)
	{
	if(inView == null) {
		return null;
	}
		String id = inView;
		Collection values = null;
		if (inProfile != null) // this is important since they may have created
								// a custom search screen or something
		{
			//id = id + "_" + inProfile.get("settingsgroup");
			String propId = "view_" + id.replace('/', '_');
			values = inProfile.getValues(propId);
			if (values != null)
			{
				id = id + "_" + values.toString(); // TODO: More specific to the user,
													// 1000 limit cache
			}
		}
		View view = null;
		if (id != null)
		{
			view = getViewCache().get(id);  //TODO: Replace with standard CacheManager
		}
		if (view != null)
		{
			return view;
		}
		XmlFile types = getViewXml(inView);
		if (types.isExist())
		{
			view = readViewElement(propdetails, types.getRoot(), inView);
			if (view != null)
			{
				view.setViewFile(types);
			}
		}

		if (view == null)
		{
			view = new View();
			view.setId(inView);
		}

		if (inProfile != null)
		{
			// Support custom values such as columns or searches
			if (values != null)
			{
				// filter out any that are not in the user values
				List existing = new ArrayList(view);
				for (Iterator iterator = existing.iterator(); iterator.hasNext();)
				{
					PropertyDetail detail = (PropertyDetail) iterator.next();
					if (values.size() > 0 && !values.contains(detail))
					{
						view.remove(detail);
					}
				}

				// add new columns
				for (Iterator iterator = values.iterator(); iterator.hasNext();)
				{
					String vid = (String) iterator.next();
					if (vid.length() > 0 && view.findDetail(vid) == null)
					{
						PropertyDetail detail = loadDetail(propdetails, types, inView, vid);
						if (detail != null)
						{
							view.add(detail);
						}
					}
				}
			}
		}

		if (view.size() == 0)
		{
			return null;
		}

		if (getViewCache().size() > 1000)
		{
			getViewCache().clear();
		}
		getViewCache().put(id, view);
		return view;

	}

	// public PropertyDetail getDetail(PropertyDetails propdetails, String
	// inView, String inFieldName, User inUser)
	// {
	// View viewdetails = getDetails(propdetails, inView, inUser);
	// }

	protected PropertyDetail loadDetail(PropertyDetails propdetails, XmlFile inViewData, String inView, String inFieldName)
	{
		PropertyDetail detail = propdetails.getDetail(inFieldName);
		if (inViewData.isExist())
		{
			if (detail != null)
			{
				Element child = inViewData.getElementById(inFieldName);
				if (child != null)
				{
					PropertyDetail local = detail.copy();
					local.setView(inView);
					local.setCatalogId(getCatalogId());
					// local.setSearchType(getS)
					local.populateViewElements(child);
					return local;
				}
			}
		}
		return detail;
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	protected Map getPropertyDetails()
	{
		if (fieldPropertyDetails == null)
		{
			fieldPropertyDetails = new HashMap();
		}

		return fieldPropertyDetails;
	}

	/**
	 * This returns the properties found in store/configuration/{inType}.xml. In
	 * other words, all the possible properties for that type.
	 * 
	 * @param The
	 *            type of data (i.e., product, catalog, item, order, etc...)
	 * @return PropertyDetails
	 */
	public PropertyDetails getPropertyDetails(String inType)
	{
		PropertyDetails details = (PropertyDetails) getPropertyDetails().get(inType);
		try
		{
			//Always load up base file first if we can
			
			//String path =  "/" + getCatalogId() + "/data/fields/" + inType + ".xml";
			String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + ".xml";

			XmlFile settings = getXmlArchive().getXml(path); // checks time
			String basesettings = "/" + getCatalogId() + "/data/fields/" + inType + ".xml";
			XmlFile basesettingsdefaults = getXmlArchive().getXml(basesettings);
			if (details != null && details.getInputFile() == settings)
			{
				fixBeanName(details,basesettingsdefaults,settings);
				return details;
			}
			log.debug("Loading " + getCatalogId() + " " + inType);
			details = new PropertyDetails(this,inType);


			if (!settings.isExist() && !basesettingsdefaults.isExist() )
			{
				if (inType.endsWith("Log"))
				{
					path = findConfigurationFile("/fields/defaultLog.xml");
					settings = getXmlArchive().getXml(path); //RELOAD
					settings.setRoot(settings.getRoot().createCopy());
				}
				else
				{
					path = findConfigurationFile("/fields/default.xml");
					settings = getXmlArchive().getXml(path); //RELOAD
					settings.setRoot(settings.getRoot().createCopy());
					settings.getRoot().addAttribute("beanname",null); //let the fixBeanName work
				}
			}

			if( basesettingsdefaults.isExist() )
			{
//				String beanname = basesettingsdefaults.getRoot().attributeValue("beanname");
//				details.setBeanName(beanname);
//				details.setPrefix(basesettingsdefaults.getRoot().attributeValue("prefix"));
//				details.setClassName(basesettingsdefaults.getRoot().attributeValue("class"));
				details.setBaseSettings(basesettingsdefaults);
			}
			fixBeanName(details,basesettingsdefaults,settings);
			setAllDetails(details, inType, settings.getContentItem().getPath(), settings.getRoot());
			getViewLabels().clear();

			// load any defaults by folder - AFTER we have loaded all the existing stuff.
			// don't overwrite anything that is here already.
			
			
			List paths = getPageManager().getChildrenPaths("/" + getCatalogId() + "/data/fields/" + inType + "/", true);
			paths.add(basesettings); //Needed?
			List datapaths = getPageManager().getChildrenPaths("/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + "/", true);
			paths.addAll(datapaths);
			
			for (Iterator iterator = paths.iterator(); iterator.hasNext();)
			{
				String defaultfile = (String) iterator.next();
				if (defaultfile.endsWith(".xml"))
				{
					XmlFile defaults = getXmlArchive().getXml(defaultfile);
					PropertyDetails extras = new PropertyDetails(this,inType);
					setAllDetails(extras, inType, defaults.getContentItem().getPath(), defaults.getRoot());
					for (Iterator iterator2 = extras.iterator(); iterator2.hasNext();)
					{
						PropertyDetail detail = (PropertyDetail) iterator2.next();
						detail.setFolderBased(true);
						PropertyDetail existing = details.getDetail(detail.getId());
						if (existing == null)
						{
							details.addDetail(detail);
						}
					}
				}
			}

			details.setInputFile(settings);
			getPropertyDetails().put(inType, details);

//			PropertyDetail name = details.getDetail("name");
//			if( name != null)
//			{
//				if( !name.isMultiLanguage() )
//				{
//					log.info(inType + " Nope");
//				}
//			}			
			return details;
		}
		catch (OpenEditException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	protected void fixBeanName(PropertyDetails inDetails,XmlFile basesettings, XmlFile settings) 
	{

		if( settings.getRoot().attributeValue("beanname") != null)
		{
			return;
		}
		if( basesettings.getRoot().attributeValue("beanname") != null)
		{
			return;
		}

		String islists = "/" + getCatalogId() + "/data/lists/" + inDetails.getId() + ".xml";
		String isfolder = "/" + getCatalogId() + "/data/lists/" + inDetails.getId() + "/";
		
		if (inDetails.getId().endsWith("Log"))
		{
			inDetails.setBeanName("dynamicLogSearcher");
		}
		else if( getPageManager().getPage(isfolder).exists())
		{
			inDetails.setBeanName("folderSearcher");
		}
		else if( getPageManager().getPage(islists).exists())
		{
			inDetails.setBeanName("listSearcher");
		}
		else
		{
			inDetails.setBeanName("dataSearcher");
		}
	}
	public PropertyDetail createDetail(String inId, String inName)
	{
		PropertyDetail detail = null;
		
		Collection all = listSearchTypes();
		for (Iterator iterator = all.iterator(); iterator.hasNext();) 
		{
			String type = (String) iterator.next();
			PropertyDetails details = getPropertyDetailsCached(type);
			detail = details.getDetail(inId);
			if( detail != null)
			{
				detail = detail.copy();
				break;
			}
		}
		if( detail == null)
		{
			detail = new PropertyDetail();
			detail.setId(inId);
			detail.setName(inName);
			detail.setEditable(true);
			detail.setIndex(true);
			detail.setStored(true);
			detail.setCatalogId(getCatalogId());
		}
		return detail;
	}
	
	
	public PropertyDetails getPropertyDetailsCached(String inType)
	{
		PropertyDetails details = (PropertyDetails) getPropertyDetails().get(inType);
		if (details == null)
		{
			return getPropertyDetails(inType);
		}
		return details;
	}
	public void savePropertyDetail(PropertyDetail inDetail, String inType, User inUser)
	{
		String path = findSavePath() + "/fields/" + inType + ".xml";
		savePropertyDetail(inDetail,path,inType,inUser);
	}
	public void updatePropertyDetail(PropertyDetail inDetail, String inType, User inUser)
	{
		String path = inDetail.getInputFilePath();
		if( path == null)
		{
			 path = findSavePath() + "/fields/" + inType + ".xml";
		}
		savePropertyDetail(inDetail,path,inType,inUser);

	}
	public void savePropertyDetail(PropertyDetail inDetail, String inPath, String inType, User inUser)
	{

		XmlFile settings = getXmlArchive().getXml(inPath);
		if (!settings.isExist())
		{
			settings = createDetailsFile(inType);
		}

		Element targetdetail = settings.getElementById(inDetail.getId());

		if (targetdetail == null)
		{
			targetdetail = settings.addNewElement();
			targetdetail.setName("property");

		}
		fillElement(targetdetail, inDetail);

		getXmlArchive().saveXml(settings, inUser);
		clearCache();

	}

	//	public void savePropertyDetails(PropertyDetails inDetails, String inType,
	//			User inUser) {
	//		XmlFile file = createDetailsFile(inDetails, inType);
	//
	//		for (Iterator iterator = inDetails.getDetails().iterator(); iterator
	//				.hasNext();) {
	//			PropertyDetail detail = (PropertyDetail) iterator.next();
	//			Element element = file.addNewElement();
	//			fillElement(inDetails.getDefaults(), element, detail);
	//		}
	//
	//		getXmlArchive().saveXml(file, inUser);
	//		clearCache();
	//	}

	private XmlFile createDetailsFile(String inType)
	{
		XmlFile file = new XmlFile();
		String path = findSavePath() + "/fields/" + inType + ".xml";
		file.setPath(path);
		Element root = DocumentHelper.createElement("properties");

		file.setRoot(root);
		file.setElementName("property");
		return file;
	}

	protected Map<String, View> getViewCache()
	{
		if (fieldViewCache == null)
		{
			fieldViewCache = new HashMap<String, View>();
		}
		return fieldViewCache;
	}

	public void clearCache()
	{
		getPropertyDetails().clear();
		getViewCache().clear();
		getViewLabels().clear();
		fieldSearchTypes = null;
	}

	public void savePropertyDetails(PropertyDetails inDetails, String inType, User inUser, String path)
	{
		XmlFile file = new XmlFile();

		file.setPath(path);
		Element root = DocumentHelper.createElement("properties");
		if( inDetails.getBaseSettings() == null) //The parent take precedence
		{
			if (inDetails.getPrefix() != null)
			{
				root.addAttribute("prefix", inDetails.getPrefix());
			}
	
			if (inDetails.getBeanName() != null)
			{
				root.addAttribute("beanname", inDetails.getBeanName());
			}
			if (inDetails.getClassName() != null)
			{
				root.addAttribute("class", inDetails.getClassName());
			}
			if (inDetails.getSearchTypes() != null)
			{
				root.addAttribute("searchtypes", inDetails.getSearchTypes());
			}
		}

		file.setRoot(root);
		file.setElementName("property");

		for (Iterator iterator = inDetails.getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if( !detail.isFolderBased() )
			{
				Element element = file.addNewElement();
				fillElement(element, detail);
			}
		}
		getXmlArchive().saveXml(file, inUser);
		clearCache();
	}

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public PropertyDetail getDataProperty(String inType, String inView, String inField, User inUser)
	{
		PropertyDetails details = getPropertyDetailsCached(inType);
		if (details == null)
		{
			return null;
		}
		View dataprops = getDetails(details, inView, inUser);
		if (dataprops != null)
		{
			return dataprops.findDetail(inField);
		}
		return null;
	}

	public void setAllDetails(PropertyDetails details, String inType, String inInputFile, Element root)
	{
		List newdetails = new ArrayList();
		Map defaults = new HashMap();

		for (Iterator iterator = root.attributeIterator(); iterator.hasNext();)
		{
			Attribute attr = (Attribute) iterator.next();
			String name = attr.getName();
			if( name != null)
			{
				name = name.trim();
			}
			String value = attr.getValue();
			if( value != null)
			{
				value = value.trim();
			}
			defaults.put(name, value);
		}
		details.setDefaults(defaults);
		for (Iterator iter = root.elementIterator("property"); iter.hasNext();)
		{
			Element element = (Element) iter.next();
			//Remove deleted ones?
			if( "true".equals(element.attributeValue("deleted") ) )
			{
				continue;
			}
			PropertyDetail d = createDetail(defaults, inInputFile, element, inType);
			newdetails.add(d);
			
		}
		// Collections.sort(newdetails);
		details.setDetails(newdetails);
	}

	public void fillElement(Element element, PropertyDetail inDetail)
	{
		element.addAttribute("id", inDetail.getId());
		element.addAttribute("externalid", inDetail.getExternalId());
		element.addAttribute("externaltype", inDetail.getExternalType());
		if (inDetail.getCatalogId() != null && !inDetail.getCatalogId().equals(getCatalogId()))
		{
			element.addAttribute("catalogid", inDetail.getCatalogId());
		}
		if (inDetail.getName() != null)
		{
			Element child = element.element("name");
			if (child != null)
			{
				element.remove(child);
				//Clean any other text?
			}

			child = element.addElement("name");

			Map languages = inDetail.getElementData().getLanguageMap("name");
			for (Iterator iterator = languages.keySet().iterator(); iterator.hasNext();)
			{
				String lang = (String) iterator.next();
				String val = (String) languages.get(lang);
				child.addElement("language").addAttribute("id", lang).addCDATA((String) val);
			}
		}
		saveBoolean(element, "index",inDetail.isIndex());
		saveBoolean(element, "keyword",inDetail.isKeyword());
		saveBoolean(element, "filter",inDetail.isFilter());
		saveBoolean(element, "editable",inDetail.isEditable());

		
		for (Iterator iterator = inDetail.getElementData().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String val = (String) inDetail.get(key);
			if (!"name".equals(key))
			{
				element.addAttribute(key, val);
			}
		}
		
		ArrayList toremove = new ArrayList();
		for (Iterator iterator = element.attributeIterator(); iterator.hasNext();) {
			Attribute attr = (Attribute) iterator.next();
			if(!inDetail.getElementData().keySet().contains(attr.getName())){
				toremove.add(attr);
			}
			
		}
		
		for (Iterator iterator = toremove.iterator(); iterator.hasNext();) {
			Attribute attr = (Attribute) iterator.next();
			element.remove(attr);
		}
		
	
		
		
		
		
		
		String type = inDetail.getDataType();
		if (type != null)
		{
			element.addAttribute("type", type);
		}

		String viewtype = inDetail.getViewType();
		if (viewtype != null)
		{
			element.addAttribute("viewtype", viewtype);
		} else{
			Attribute viewattr = element.attribute("viewtype");
			if(viewattr != null){
				element.remove(viewattr);
			}
			 viewattr = element.attribute("rendertype");
			if(viewattr != null){
				element.remove(viewattr);
			}
			
			
		}

		String datatype = inDetail.getDataType();
		if (datatype != null)
		{
			element.addAttribute("datatype", datatype);
		} else{
			Attribute viewattr = element.attribute("datatype");
			if(viewattr != null){
				element.remove(viewattr);
			}			
		}
		//Cleanup and standardize
		
		
		
		
		
		Attribute typeattr = element.attribute("type");
		if(typeattr != null){
			element.remove(typeattr);
		}
		
		
		
	}

	private void saveBoolean(Element inElement, String inId, boolean inIndex)
	{
		if( inIndex )
		{
			inElement.addAttribute("index", "true");
		}
		else
		{
			Attribute atr = inElement.attribute(inId);
			if( atr != null)
			{
				inElement.remove(atr);
			}	
		}
	}


	protected PropertyDetail createDetail(Map defaults, String inInputFile, Element element, String inType)
	{
		PropertyDetail d = new PropertyDetail();
		d.setElementData(new ElementData(element));
		d.setTextLabelManager(getTextLabelManager());
		d.setInputFilePath(inInputFile);
		//		for (Iterator iterator = defaults.keySet().iterator(); iterator.hasNext();)
		//		{
		//			String key = (String) iterator.next();
		//			String value = (String) defaults.get(key);
		//			d.setValue(key, value);
		//		}
		
		//
		//populateViewElements(element, d);

		d.setCatalogId(getCatalogId());
		d.setSearchType(inType);

		if (d.isViewType("list") && d.getListId() == null)
		{
			d.setListId(d.getId());
		}
		return d;
	}

	protected List listFilesByFolderType(String inFolderType, boolean subdirectories)
	{
		// lists, views, fields
		String inPath = "/" + getCatalogId() + "/data/" + inFolderType + "/";
		List basepaths = getPageManager().getChildrenPaths(inPath, true);

		Set set = new HashSet();
		addFolder(inPath, "", set, subdirectories);
		String datapath = "/WEB-INF/data/" + getCatalogId() + "/" + inFolderType;
		addFolder(datapath, "" ,set, subdirectories);
		
		// // We don't want this in a loop or to follow a chain.
		List sorted = new ArrayList(set);
		// if (includeExtensions)
		// {
		// HitTracker extensions = (HitTracker)
		// getSearcherManager().getList(getCatalogId(), "dataextensions");
		//
		// for (Iterator iterator = extensions.iterator(); iterator.hasNext();)
		// {
		// Data remotecatalog = (Data) iterator.next();
		// String catalogid = remotecatalog.get("catalogid");
		// PropertyDetailsArchive archive =
		// getSearcherManager().getPropertyDetailsArchive(catalogid);
		// List remotevalues = archive.listFilesByFolderType(inFolderType,
		// false);
		// sorted.addAll(remotevalues);
		// }
		// }
		Collections.sort(sorted);
		return sorted;

	}
	
	protected void addFolder(String inPath, String sub, Collection files, boolean subdirectories)
	{
		List children = getPageManager().getChildrenPaths(inPath, true);
		for (Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();

			String type = sub;
			if(type.length() > 0)
			{
				type = type  + "/" + PathUtilities.extractPageName(path);
			}
			else
			{
				type = PathUtilities.extractPageName(path);
			}

			ContentItem file = getPageManager().getRepository().getStub(path);
			if( subdirectories && file.isFolder() )
			{
				addFolder(inPath + "/" + file.getName(), type, files, subdirectories);
			}
			else
			{
				if (!type.startsWith("_"))
				{
					files.add(type);
				}
			}	
		}

	}

	public List listSearchTypes()
	{
		List fields = listFilesByFolderType("fields", false);
		HashSet all = new HashSet(fields);
		List lists = listFilesByFolderType("lists", false);
		all.addAll(lists);
		List sorted = new ArrayList(all);
		Collections.sort(sorted);
		fieldSearchTypes = sorted;
		return fieldSearchTypes;
	}
	public List getSearchTypes()
	{
		if( fieldSearchTypes == null)
		{
			fieldSearchTypes = listSearchTypes();
		}
		return fieldSearchTypes;
	}

	public List findChildTables()
	{
		if (fieldChildTables == null)
		{
			fieldChildTables = new ArrayList();
			List searchtypes = listSearchTypes();
			for (Iterator iterator = searchtypes.iterator(); iterator.hasNext();)
			{
				String type = (String) iterator.next();
				PropertyDetails details = getPropertyDetailsCached(type);
				PropertyDetail parent = details.getDetail("_parent");
				if (parent != null)
				{
					fieldChildTables.add(details);
				}

			}

		}
		else
		{
			return fieldChildTables;
		}

		return new ArrayList();
	}

	public List<String> findChildTablesNames()
	{

		if (fieldChildTableNames == null)
		{
			fieldChildTableNames = new ArrayList();
			List searchtypes = listSearchTypes();
			for (Iterator iterator = searchtypes.iterator(); iterator.hasNext();)
			{
				String type = (String) iterator.next();
				PropertyDetails details = getPropertyDetailsCached(type);
				PropertyDetail parent = details.getDetail("_parent");
				if (parent != null)
				{
					fieldChildTableNames.add(type);
				}
			}

			return fieldChildTableNames;
		}
		else
		{
			return fieldChildTableNames;
		}

	}

	public List listViewTypes()
	{
		return listFilesByFolderType("views/", false);
	}

	public List listViews(String inViewType)
	{
		return listFilesByFolderType("views/" + inViewType, false);
	}

	public Data getView(String inType, String inViewPath)
	{
		// Guess the type is not used. the inview has the type in the path name?
		return getViewXml(inViewPath);
	}

	public String getViewLabel(String inView)
	{
		String usage = (String) getViewLabels().get(inView);
		if (usage == null)
		{
			Data view = getViewXml(inView);
			usage = view.get("usagelabel");
			if (usage == null)
			{
				usage = "";
			}
			getViewLabels().put(inView, usage);
		}
		return usage;
	}

	protected Map getViewLabels()
	{
		if (fieldViewLabels == null)
		{
			fieldViewLabels = new HashMap();
		}
		return fieldViewLabels;
	}

	public View readViewElement(PropertyDetails inDetails, Element inElem, String inViewName)
	{
		View view = new View();
		view.setId(inViewName);
		for (Iterator iter = inElem.elementIterator(); iter.hasNext();)
		{
			Element elem = (Element) iter.next();
			if (elem.getName().equals("section"))
			{
				View child = readViewElement(inDetails, elem, inViewName);
				if (child != null)
				{
					child.setTitle(elem.attributeValue("title"));
					view.add(child);
				}
			}
			else if (elem.getName().equals("property"))
			{
				String key = elem.attributeValue("id");
				PropertyDetail detail = inDetails.getDetail(key);
				if (detail != null)
				{
					if( detail.isDeleted())
					{
						continue;
					}
					PropertyDetail local = detail.copy();
					local.setView(inViewName);
					local.populateViewElements(elem);
					view.add(local);
				}
			}
		}
		return view;
	}

	public void saveView(View inView, User inUser)
	{
		XmlFile file = getXmlArchive().getXml(findSavePath() + "/views/" + inView.getId() + ".xml");
		file.clear();

		Element root = file.getRoot();
		appendValues(root, inView);
		getXmlArchive().saveXml(file, inUser);
		clearCache();
	}

	protected void appendValues(Element inRoot, View inView)
	{
		for (Iterator iterator = inView.iterator(); iterator.hasNext();)
		{
			Object object = (Object) iterator.next();
			if (object instanceof PropertyDetail)
			{
				Element child = inRoot.addElement("property");
				PropertyDetail prop = (PropertyDetail) object;
				child.addAttribute("id", prop.getId());
			}
			else if (object instanceof View)
			{
				Element child = inRoot.addElement("section");
				View view = (View) object;
				child.addAttribute("title", view.getTitle());
				appendValues(child, view);
			}

		}

	}

	/**
	 * This is cached
	 * 
	 * @param inSearchType
	 * @param inView
	 * @param inPropertyid
	 * @param inUserProfile
	 * @return
	 */
	public PropertyDetail getDetail(String inSearchType, String inView, String inPropertyid, UserProfile inUserProfile)
	{
		PropertyDetail detail = null;
		if (inView == null)
		{
			PropertyDetails details = getPropertyDetailsCached(inSearchType);
			detail = details.getDetail(inPropertyid);
			return detail;
		}

		View view = getView(inSearchType, inView, inUserProfile);
		if (view != null)
		{
			detail = view.findDetail(inPropertyid);
		}
		else
		{
			PropertyDetails details = getPropertyDetailsCached(inSearchType);
			detail = details.getDetail(inPropertyid);
		}
		return detail;
	}

	public void clearCustomSettings(String inSearchType)
	{
		String path = findConfigurationFile("/fields/" + inSearchType + ".xml");
		Page found = getPageManager().getPage(path);
		getPageManager().removePage(found);

		getPropertyDetails().remove(inSearchType);
	}

	public void reloadSettings(String inSearchType)
	{
		getPropertyDetails().remove(inSearchType);
	}

	public Map findSearchersWithDetail(String inDetail)
	{
		HashMap types = new HashMap();
		for (Iterator iterator = listSearchTypes().iterator(); iterator.hasNext();)
		{
			String type = (String) iterator.next();
			PropertyDetails details = getPropertyDetails(type);
			PropertyDetail detail = details.getDetail(inDetail);

			if (detail != null)
			{
				types.put(type, detail);
			}

		}
		return types;
	}

	public boolean convertAll(String inSearcher, String inDetail)
	{
		Map types = findSearchersWithDetail(inDetail);
		PropertyDetails details = getPropertyDetails(inSearcher);
		PropertyDetail detail = details.getDetail(inDetail);
		for (Iterator iterator = types.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			PropertyDetails targetdetails = getPropertyDetails(key);
			targetdetails.removeDetail(inDetail);
			detail.setSearchType(null);
			targetdetails.addDetail(detail);
			savePropertyDetail(detail, key, null);
		}
		return true;
	}

	public boolean makeLegacy(String inSearcher, String inDetail)
	{
		Map types = findSearchersWithDetail(inDetail);
		PropertyDetails details = getPropertyDetails(inSearcher);
		PropertyDetail detail = details.getDetail(inDetail);
		detail.setId(inSearcher + inDetail);
		detail.setProperty("legacy", inDetail);
		savePropertyDetail(detail, inSearcher, null);
		//savePropertyDetails(details, inSearcher, null);
		PropertyDetail old = details.getDetail(inDetail);
		deletePropertyDetail(old, inSearcher, null);

		//should we search and reset any list ids?
		for (Iterator iterator = listSearchTypes().iterator(); iterator.hasNext();)
		{
			String type = (String) iterator.next();
			PropertyDetails searchdetails = getPropertyDetails(type);
			List alldetails = searchdetails.getDetails();
			for (Iterator iterator2 = searchdetails.iterator(); iterator2.hasNext();)
			{
				PropertyDetail anotherdetail = (PropertyDetail) iterator2.next();
				if (anotherdetail.isList() && inDetail.equals(anotherdetail.get("listid")))
				{
					anotherdetail.setListId(detail.getId());
					savePropertyDetail(anotherdetail, type, null);
				}
			}

		}

		return true;
	}

	public void deletePropertyDetail(PropertyDetail inDetail, String inSearchtype, User inUser)
	{

		String path = findSavePath() + "/fields/" + inSearchtype + ".xml";
		XmlFile settings = getXmlArchive().loadXmlFile(path);
		if (!settings.isExist())
		{
			return;
		}

		Element targetdetail = settings.getElementById(inDetail.getId());

		if (targetdetail != null)
		{
			settings.deleteElement(targetdetail);

		}

		getXmlArchive().saveXml(settings, inUser);
		clearCache();

	}

	public String findSavePath()
	{
//		//PLEASE leave this here :)
//		Searcher catalogsettings = getSearcherManager().getSearcher(getCatalogId(), "catalogsettings");
//		Data savepath = (Data) catalogsettings.searchById("detailsavepath");
//		if(savepath != null) {
//			return savepath.get("value"); 
//		}
		//Thanks!
		
		if( "base".equals( getSaveTo()))
		{
			return "/WEB-INF/base/entermedia/catalog/data/";
		}	
		if( "catalog".equals( getSaveTo()))
		{
			return "/" + getCatalogId() + "/data";
		}
	
		
		
		return "/WEB-INF/data/" + getCatalogId();
	}

	
	
	public String findConfigurationFile(String inPath)
	{
		String path = "/WEB-INF/data/" + getCatalogId() + inPath;
		if (!getPageManager().getRepository().doesExist(path))
		{
			path = "/" + getCatalogId() + "/data" + inPath;
		}
		return path;
	}

	public void addToView(Searcher inSearcher, String viewpath , String inNewField)
	{
		Data view = getView(inSearcher.getSearchType(), viewpath);

		XmlFile file = (XmlFile)view;
		String viewbase = null;
		
//			if(archive != null) {
//				viewbase = archive.getCatalogSettingValue("viewbase");  //TODO: Add setting in the archive
//			}
//			if(viewbase == null) {
			 viewbase = "/WEB-INF/data/" + inSearcher.getCatalogId()+ "/views/";
//	
//			}
		String path = viewbase + viewpath + ".xml";
		file.setPath(path);
		file.setElementName("property");

		Element element = file.addNewElement();
		element.addAttribute("id", inNewField);
		element.clearContent();
		getXmlArchive().saveXml(file, null);
		getViewCache().clear();// clearCache(); //Only clear this type
			
	}
	
	public void removeFromView(Searcher inSearcher, String inViewpath, String indetailid)
	{
		Data view = getView(inSearcher.getSearchType(), inViewpath);

		XmlFile file = (XmlFile)view;
		String viewbase = "/WEB-INF/data/" + inSearcher.getCatalogId()+ "/views/";
		String path = viewbase + inViewpath + ".xml";
		file.setPath(path);
		file.setElementName("property");

		Element element = loadViewElement(file, indetailid);
		file.deleteElement(element);

		if (file.getRoot().elements().size() == 0)
		{
			getXmlArchive().deleteXmlFile(file);
		}
		else
		{
			getXmlArchive().saveXml(file, null);
		}

		getXmlArchive().saveXml(file, null);
		getViewCache().clear();// clearCache(); //Only clear this type		
	}

	public void saveView(Searcher inSearcher, String inViewpath, String[] inSortedIds)
	{
		if (inSortedIds == null) {
			throw new OpenEditException("Missing sort list ids");
		}
		Data view = getView(inSearcher.getSearchType(), inViewpath);

		XmlFile file = (XmlFile)view;
		String viewbase = "/WEB-INF/data/" + inSearcher.getCatalogId()+ "/views/";
		String path = viewbase + inViewpath + ".xml";
		file.setPath(path);
		file.setElementName("property");

		List tosave = new ArrayList();
		for (int i = 0; i < inSortedIds.length; i++)
		{
			//Element sourceelement = file.getElementById();
			String id = inSortedIds[i];
			Element sourceelement = loadViewElement(file, id);
			if (sourceelement != null)
			{
				sourceelement = (Element)sourceelement.clone();
				tosave.add(sourceelement);
			}
		}
		if (tosave.isEmpty())
		{
			throw new OpenEditException("Should not be removing all fields");
		}

		file.getElements().clear();
		file.getElements().addAll(tosave);
		
		getXmlArchive().saveXml(file, null);
		getViewCache().clear();// clearCache(); //Only clear this type		
		
	}

	protected Element loadViewElement(XmlFile file, String toremove)
	{
		Element element = file.getElementById(toremove);
		if (element == null && toremove.contains("."))
		{
			toremove = toremove.substring(toremove.indexOf(".") + 1, toremove.length());
			element = file.getElementById(toremove);
		}
		return element;
	}

}
