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
import org.openedit.ModuleManager;
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
	protected TextLabelManager fieldTextLabelManager;
	protected PageManager fieldPageManager;
	protected SearcherManager fieldSearcherManager;
	protected Map<String, ViewFieldList> fieldViewCache;
	protected Map fieldPropertyDetails;
	protected List fieldChildTables;
	protected List fieldChildTableNames;
	protected String fieldSaveTo = "data"; //base,catalog,data
	protected List fieldSearchTypes;
	
	protected ModuleManager fieldModuleManager;

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

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

	
	public ViewFieldList getViewFields(String inSearchType, String inViewId, UserProfile inProfile)
	{
		Data viewdata = getSearcherManager().getCachedData(getCatalogId(), "view", inViewId);
		PropertyDetails details = getPropertyDetailsCached(inSearchType);
		ViewFieldList fields = getViewFields(details, viewdata, inProfile);
		return fields;
	}

	public ViewFieldList getViewFields(PropertyDetails propdetails, Data inViewData, UserProfile inProfile)
	{
		if(inViewData == null) {
			log.error("No viewdata found");
			return null;
		}
		String id = inViewData.getId();
		Collection values = null;
		if (inProfile != null) // this is important since they may have created
								// a custom search screen or something
		{
			//id = id + "_" + inProfile.get("settingsgroup");
			String propId = "view_" + id;
			values = inProfile.getValues(propId);
			if (values != null)
			{
				id = id + "_" + values.toString(); // TODO: More specific to the user,
													// 1000 limit cache
			}
		}
		ViewFieldList details = null;
		if (id != null)
		{
			details = getViewCache().get(id);  //TODO: Replace with standard CacheManager
		}
		if (details != null)
		{
			return details;
		}
		XmlFile types = getViewXml(inViewData);
		if (types.isExist())
		{
			details = readViewElement(propdetails, types.getRoot());
			if (details != null && !details.isEmpty())
			{
				details.setViewFile(types);
			}
		}

		if (details == null || details.isEmpty())
		{
			details = new ViewFieldList();
			details.setId(inViewData.getId());  //Not needed
		}

		if (inProfile != null)
		{
			// Support custom values such as columns or searches
			if (values != null)
			{
				// filter out any that are not in the user values
				List existing = new ArrayList(details);
				for (Iterator iterator = existing.iterator(); iterator.hasNext();)
				{
					PropertyDetail detail = (PropertyDetail) iterator.next();
					if (values.size() > 0 && !values.contains(detail))
					{
						details.remove(detail);
					}
				}

				// add new columns
				for (Iterator iterator = values.iterator(); iterator.hasNext();)
				{
					String vid = (String) iterator.next();
					if (vid.length() > 0 && details.findDetail(vid) == null)
					{
						PropertyDetail detail = loadDetail(propdetails, types, vid);
						if (detail != null)
						{
							details.add(detail);
						}
					}
				}
			}
		}

		if (details.size() == 0)
		{
			return null;
		}

		if (getViewCache().size() > 1000)
		{
			getViewCache().clear();
		}
		getViewCache().put(id, details);
		return details;

	}

	public XmlFile getViewXml(Data inViewData)
	{
		
		String moduleid = inViewData.get("moduleid");
		if( moduleid == null)
		{
			String backuppath = findConfigurationFile("/views/" + inViewData.getId() + ".xml");
			XmlFile file = getXmlArchive().getXml(backuppath);
			return file;

		}

		//First Data
		String inViewPath = moduleid + "/" + inViewData.getId();
		String readwritepath = "/WEB-INF/data/" + getCatalogId() + "/views/" + inViewPath + ".xml";
		XmlFile file = getXmlArchive().getXml(readwritepath);  //This comes from base page
		
		if( !file.isExist() )
		{
			//Look in base
			String backuppath = findConfigurationFile("/views/" + inViewPath + ".xml");
			file = getXmlArchive().getXml(backuppath);

			if( !file.isExist() )
			{
				//Replace the name with default
				String name = inViewData.getId();
				if (name.length() > moduleid.length())
				{
					name = name.substring(moduleid.length());
					String path = "/" + getCatalogId() + "/data/views/defaults/" + name + ".xml";
					file = getXmlArchive().getXml(path);
				}
				//Random anything
				if( !file.isExist() )
				{
					log.error("No default view found " + name);
					String path = "/" + getCatalogId() + "/data/views/defaults/resultstable.xml";
					file = getXmlArchive().getXml(path);
				}
			}
			file.setPath(readwritepath); //In case they save it
		}
		
		return file;
	}

	// public PropertyDetail getDetail(PropertyDetails propdetails, String
	// inView, String inFieldName, User inUser)
	// {
	// View viewdetails = getDetails(propdetails, inView, inUser);
	// }

	protected PropertyDetail loadDetail(PropertyDetails propdetails, XmlFile inViewData, String inFieldName)
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
			//Check cache
			String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + ".xml";
			XmlFile dataxmlfile = getXmlArchive().getXml(path); // checks time
			
			String basedataxmlfile = "/" + getCatalogId() + "/data/fields/" + inType + ".xml";
			XmlFile basesettingsdefaults = getXmlArchive().getXml(basedataxmlfile);
			if (details != null && details.getInputFile() == dataxmlfile) //Cache Check. Same instance
			{
				fixBeanName(details, inType, basesettingsdefaults, dataxmlfile);
				return details;
			}
			
			//log.debug("Loading " + getCatalogId() + " " + inType);
			
			HashMap allfields = new HashMap();
			
			if (!dataxmlfile.isExist() && !basesettingsdefaults.isExist() )
			{
				if (inType.endsWith("Log"))
				{
					path = findConfigurationFile("/fields/defaultLog.xml");
					dataxmlfile = getXmlArchive().getXml(path); //RELOAD
					dataxmlfile.setRoot(dataxmlfile.getRoot().createCopy());
				}
				else
				{
					path = findConfigurationFile("/fields/default.xml");
					dataxmlfile = getXmlArchive().getXml(path); //RELOAD
					dataxmlfile.setRoot(dataxmlfile.getRoot().createCopy());
					dataxmlfile.getRoot().addAttribute("beanname",null); //let the fixBeanName work
				}
				
			}
			else if(!basesettingsdefaults.isExist())
			{
				String beanname = dataxmlfile.getRoot().attributeValue("beanname");
				if( beanname == null || beanname.equals("listSearcher"))
				{
					String listpath = findConfigurationFile("/fields/default.xml");
					basesettingsdefaults = getXmlArchive().getXml(listpath);
					
				}
			}

			details = new PropertyDetails(this,inType); //Start fresh

			//Go from specific to general details
			if( dataxmlfile.getContentItem().exists() ) //data exact
			{
				loadDetails(details, allfields, inType, dataxmlfile.getContentItem().getPath(), dataxmlfile.getRoot(), false);    //data exact
			}
			
			if( basesettingsdefaults.isExist() )
			{
				loadDetails(details, allfields, inType, basesettingsdefaults.getContentItem().getPath(), basesettingsdefaults.getRoot(), false);    //Base data exact
			}
			details.setBaseSettings(basesettingsdefaults);  //For bean name etc
			fixBeanName(details,inType, basesettingsdefaults, dataxmlfile);
			
			// load any defaults by folder - AFTER we have loaded all the existing stuff.
			// don't overwrite anything that is here already.
			List datapaths = getPageManager().getChildrenPaths("/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + "/", true); //data FOLDERS.. Is this used?
			for (Iterator iterator = datapaths.iterator(); iterator.hasNext();)
			{
				String baseandfolderfiles = (String) iterator.next();
				if (baseandfolderfiles.endsWith(".xml"))
				{
					XmlFile defaults = getXmlArchive().getXml(baseandfolderfiles);
					loadDetails(details, allfields, inType, defaults.getContentItem().getPath(), defaults.getRoot(), true);
				}
			}

			List basefolders = getPageManager().getChildrenPaths("/" + getCatalogId() + "/data/fields/" + inType + "/", true); //This is base FOLDERS
			for (Iterator iterator = basefolders.iterator(); iterator.hasNext();)
			{
				String baseandfolderfiles = (String) iterator.next();
				if (baseandfolderfiles.endsWith(".xml"))
				{
					XmlFile defaults = getXmlArchive().getXml(baseandfolderfiles);
					loadDetails(details, allfields, inType, defaults.getContentItem().getPath(), defaults.getRoot(), true);
				}
			}
			
			for (Iterator iterator = allfields.values().iterator(); iterator.hasNext();)
			{
				PropertyDetail detail = (PropertyDetail) iterator.next();
				//Dont add it twice?
				
				if(!detail.isDeleted())
				{
					details.addDetail(detail);
				}
			}
			details.setInputFile(dataxmlfile);
			
			Collections.sort( details.getDetails() );
			
			getPropertyDetails().put(inType, details);
			
			//Sort em

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

	protected void fixBeanName(PropertyDetails inDetails, String inSearchtype, XmlFile basesettings, XmlFile settings) 
	{
		if( basesettings != null && basesettings.getRoot() != null)
		{
			String enforcedname = basesettings.getRoot().attributeValue("enforcebeanname");
			if(enforcedname != null) {
				 inDetails.setBeanName(enforcedname);
				 return;
			}
		}
		
		
		String beanName = null;
		if( getModuleManager().contains(getCatalogId(), inSearchtype + "Searcher") ) //this might be a lookup
		{
			beanName = inSearchtype + "Searcher";
		}	
		else if( getModuleManager().contains(inSearchtype + "Searcher"))
		{
			beanName = inSearchtype + "Searcher";
		}
		if( beanName == null)
		{
			beanName =settings.getRoot().attributeValue("beanname");
			if( beanName != null)
			{
				return;
			}
		}
		if (beanName != null)
		{
			inDetails.setBeanName(beanName);
			return;
		}
		String islists = "/" + getCatalogId() + "/data/lists/" + inDetails.getId() + ".xml";
		String isfolder = "/" + getCatalogId() + "/data/lists/" + inDetails.getId() + "/";
		
		String isDatalists = "/WEB-INF/data/" + getCatalogId() + "/lists/" + inDetails.getId() + ".xml";
		String isDatafolder = "/WEB-INF/data/" + getCatalogId() + "/lists/" + inDetails.getId() + "/";
		
		if (inDetails.getId().endsWith("Log"))
		{
			beanName = "dynamicLogSearcher";
		}
		else if( getPageManager().getPage(isfolder).exists())
		{
			beanName = "folderSearcher";
		}
		else if( getPageManager().getPage(islists).exists())
		{
			beanName = "listSearcher";
		}
		else if( getPageManager().getPage(isDatafolder).exists())
		{
			beanName = "folderSearcher";
		}
		else if( getPageManager().getPage(isDatalists).exists())
		{
			beanName = "listSearcher";
		}
		else
		{
			beanName = basesettings.getRoot().attributeValue("beanname");
			if (beanName == null)
			{
				beanName = "dataSearcher";
			}
		}
		inDetails.setBeanName(beanName);
	}
	
	public PropertyDetail createDetail(String inSearchtype, String inId, String inName)
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
			PropertyDetails details = getPropertyDetailsCached(inSearchtype);
			detail = details.createDetail(inId);
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

	protected Map<String, ViewFieldList> getViewCache()
	{
		if (fieldViewCache == null)
		{
			fieldViewCache = new HashMap<String, ViewFieldList>();
		}
		return fieldViewCache;
	}

	public void clearCache()
	{
		getPropertyDetails().clear();
		getViewCache().clear();
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

	public void loadDetails(PropertyDetails inDetails, Map tosave, String inType, String inInputFile, Element root, boolean folderbased)
	{
		for (Iterator iter = root.elementIterator("property"); iter.hasNext();)
		{
			Element element = (Element) iter.next();
			String id = element.attributeValue("id");
			if( tosave.get(id) != null)
			{
				continue;
			}
			PropertyDetail detail = createDetail(inDetails, inInputFile, element, inType);
			detail.setFolderBased(folderbased);
			tosave.put(detail.getId(),detail);
		}
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


	protected PropertyDetail createDetail(PropertyDetails inDetails, String inInputFile, Element element, String inType)
	{
		PropertyDetail d = new PropertyDetail();
		d.setElementData(new ElementData(element, inDetails));
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
				
				if (!type.startsWith("_") && !type.isEmpty()) //ignore .DS_Store
				{	
					files.add(type);
				}
			}	
		}

	}

	public List<String> listSearchTypes()
	{
		List fields = listFilesByFolderType("fields", false);
		HashSet all = new HashSet(fields);
		List lists = listFilesByFolderType("lists", false);
		all.addAll(lists);
		
		Collection remote = getSearcherManager().getList(getCatalogId(), "searchtypes");
		for (Iterator iterator = remote.iterator(); iterator.hasNext();)
		{
			Data other = (Data) iterator.next();
			all.add(other.getId());  //Users and Groups
		}
		
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

	public ViewFieldList readViewElement(PropertyDetails inDetails, Element inElem)
	{
		ViewFieldList view = new ViewFieldList();
		for (Iterator iter = inElem.elementIterator(); iter.hasNext();)
		{
			Element elem = (Element) iter.next();
			if (elem.getName().equals("section"))
			{
				ViewFieldList child = readViewElement(inDetails, elem);
				if (child != null)
				{
					child.setTitle(elem.attributeValue("title"));
					view.add(child);
				}
			}
			else
			{
				if( "true".equals( elem.attributeValue("deleted")) )
				{
					continue;
				}
				String key = elem.attributeValue("id");
				PropertyDetail detail = inDetails.getDetail(key);
				if (detail != null)
				{
					PropertyDetail local = detail.copy();
					local.populateViewElements(elem);
					view.add(local);
				}
			}
		}
		return view;
	}

	public void saveView(ViewFieldList inView, User inUser)
	{
		XmlFile file = getXmlArchive().getXml(findSavePath() + "/views/" + inView.getId() + ".xml");
		file.clear();

		Element root = file.getRoot();
		appendValues(root, inView);
		getXmlArchive().saveXml(file, inUser);
		clearCache();
	}

	protected void appendValues(Element inRoot, ViewFieldList inView)
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
			else if (object instanceof ViewFieldList)
			{
				Element child = inRoot.addElement("section");
				ViewFieldList view = (ViewFieldList) object;
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
	public PropertyDetail getDetail(String inSearchType, String inViewId, String inDetailId, UserProfile inUserProfile)
	{
		if (inViewId != null)
		{
			ViewFieldList view = getViewFields(inSearchType, inViewId, inUserProfile);
			if (view != null)
			{
				PropertyDetail detail = view.findDetail(inDetailId);
				return detail;
			}
		}

		PropertyDetails details = getPropertyDetailsCached(inSearchType);
		PropertyDetail detail = details.getDetail(inDetailId);
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

	public void addToView(Data inViewData , String inNewField)
	{
		XmlFile file = getViewXml(inViewData);

		Element element = file.addNewElement();
		element.addAttribute("id", inNewField);
		element.clearContent();
		getXmlArchive().saveXml(file, null);
		getViewCache().clear();// clearCache(); //Only clear this type
			
	}
	
	public void removeFromView(Data inViewData, String indetailid)
	{
		XmlFile file = getViewXml(inViewData);

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

	public void saveView(Data inViewData, String[] inSortedIds)
	{
		if (inSortedIds == null) {
			throw new OpenEditException("Missing sort list ids");
		}
		XmlFile file = getViewXml(inViewData);

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


	public Collection<String> listLiveTables()
	{
		Collection<String> types = listSearchTypes();
		Set<String> names = new HashSet();

		Collection<String> livetypes  = getSearcherManager().getNodeManager(getCatalogId()).getMappedTypes(getCatalogId());

		for (Iterator iterator = types.iterator(); iterator.hasNext();)
		{
			String tablename = (String) iterator.next();
			if( !livetypes.contains(tablename) )
			{
				continue;
			}
			names.add(tablename);
		}
		return names;
	}
}
