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

	public List getStoredProperties(String inType)
	{
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null)
		{
			return Collections.EMPTY_LIST;
		}
		return details.findStoredProperties();
	}

	public String getConfigurationPath(String inPath)
	{
		String path = "/WEB-INF/data/" + getCatalogId() + inPath;
		if (!getPageManager().getRepository().doesExist(path))
		{
			path = "/" + getCatalogId() + "/data" + inPath;
			// if (!getPageManager().getPage(path).exists())
			// {
			// /*
			// // now check the extensions
			// if (!(path.contains("dataextensions") ||
			// path.contains("translation")))
			// {
			// HitTracker hits = getSearcherManager().getList(getCatalogId(),
			// "dataextensions");
			// for (Iterator iterator = hits.iterator(); iterator.hasNext();)
			// {
			// Data hit = (Data) iterator.next();
			// String catalogid = hit.get("catalogid");
			// String remotepath = "/" + catalogid + "/data/" + inPath;
			// if (getPageManager().getPage(remotepath).exists())
			// {
			// return remotepath;
			// }
			// }
			// }
			// */
			// // legacy support
			// if (inPath.startsWith("/fields/"))
			// {
			// inPath = inPath.replace(".xml", "properties.xml");
			// inPath = inPath.replace("/fields/", "/");
			// }
			// path = "/" + getCatalogId() + "/configuration" + inPath;
			//
			// }
		}
		return path;
	}

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

	protected XmlFile getViewXml(String inView)
	{
		String path = getConfigurationPath("/views/" + inView + ".xml");
		XmlFile file = getXmlArchive().getXml(path);
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
		String id = inView;
		Collection values = null;
		if (inProfile != null) // this is important since they may have created
								// a custom search screen or something
		{
			id = id + "_" + inProfile.get("settingsgroup");
			String propId = "view_" + inView.replace('/', '_');
			values = inProfile.getValues(propId);
			if (values != null)
			{
				id = id + "_" + values.toString(); // More specific to the user,
													// 1000 limit cache
			}
		}
		View view = null;
		if (id != null)
		{
			view = getViewCache().get(id);
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
						else
						{
							//find the detail
							if(vid.contains("."))
							{
								String[] type = vid.split("\\.");
								
								PropertyDetails otherdetails = getPropertyDetails(type[0]);
								PropertyDetail shareddetail = otherdetails.getDetail(type[1]);
								if( shareddetail != null)
								{
									PropertyDetail local = shareddetail.copy();
									view.add(local);
								}	
							}
							
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
		// String level = null;//
		// if (inUser != null)
		// {
		// String level = (String) inUser.getProperty("datalevel");
		// }
		// XmlFile types = null;
		// if (level != null)
		// {
		// String path = getConfigurationPath("/views/" + inView + level +
		// ".xml");
		// types = getXmlArchive().getXml(path);
		// }
		// if (types == null || !types.isExist())
		// {
		// String path = getConfigurationPath("/views/" + inView + ".xml");
		// types = getXmlArchive().getXml(path);
		// }
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
					populateViewElements(child, local);
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
			String path = getConfigurationPath("/fields/" + inType + ".xml");

			XmlFile settings = getXmlArchive().loadXmlFile(path); // checks time
																	// stamp.
																	// returns
																	// null if
																	// changed
			if (details != null && details.getInputFile() == settings)
			{
				return details;
			}
			log.debug("Loading " + getCatalogId() + " " + inType);
			settings = getXmlArchive().getXml(path);

			if (!settings.isExist() && !path.contains("dataextensions"))
			{
				if (inType.endsWith("Log"))
				{
					path = getConfigurationPath("/fields/defaultLog.xml");
				}
				else
				{
					path = getConfigurationPath("/fields/default.xml");
				}
				// This should not happen as well
				settings = getXmlArchive().getXml(path);
			}
			details = new PropertyDetails(inType);
			if (settings.isExist())
			{
				setAllDetails(details, inType, settings.getRoot());
				getViewLabels().clear();

			}

			// load any defaults - AFTER we have loaded all the existing stuff.
			// don't overwrite anything that is here already.

			List paths = getPageManager().getChildrenPaths("/" + getCatalogId() + "/data/fields/" + inType + "/", true);

			paths.add("/" + getCatalogId() + "/data/fields/" + inType + ".xml");
			for (Iterator iterator = paths.iterator(); iterator.hasNext();)
			{

				String defaultfile = (String) iterator.next();
				if (defaultfile.endsWith(".xml"))
				{
					XmlFile defaults = getXmlArchive().loadXmlFile(defaultfile);
					PropertyDetails extras = new PropertyDetails(inType);
					setAllDetails(extras, inType, defaults.getRoot());
					for (Iterator iterator2 = extras.iterator(); iterator2.hasNext();)
					{
						PropertyDetail detail = (PropertyDetail) iterator2.next();
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

			return details;
		}
		catch (OpenEditException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
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

		String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + ".xml";
		XmlFile settings = getXmlArchive().loadXmlFile(path);
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
		String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + ".xml";
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
	}

	public void savePropertyDetails(PropertyDetails inDetails, String inType, User inUser, String path)
	{
		XmlFile file = new XmlFile();

		file.setPath(path);
		Element root = DocumentHelper.createElement("properties");
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

		file.setRoot(root);
		file.setElementName("property");

		for (Iterator iterator = inDetails.getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			Element element = file.addNewElement();
			fillElement(element, detail);
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

	public void setAllDetails(PropertyDetails details, String inType, Element root)
	{
		List newdetails = new ArrayList();
		Map defaults = new HashMap();

		for (Iterator iterator = root.attributeIterator(); iterator.hasNext();)
		{
			Attribute attr = (Attribute) iterator.next();
			String name = attr.getName();
			String value = attr.getValue();
			defaults.put(name, value);
		}
		details.setDefaults(defaults);
		for (Iterator iter = root.elementIterator("property"); iter.hasNext();)
		{
			Element element = (Element) iter.next();

			PropertyDetail d = createDetail(defaults, element, inType);

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
			if(child != null){
				element.remove(child);
			}

			
			 child = element.addElement("name");
			
			Map languages = inDetail.getElementData().getLanguageMap("name");
			for (Iterator iterator = languages.keySet().iterator(); iterator.hasNext();)
			{
				String lang = (String) iterator.next();
				String val = (String)languages.get(lang);
				child.addElement("language").addAttribute("id",lang).addCDATA((String)val);
			}
			
		
		}

		element.addAttribute("index", String.valueOf( inDetail.isIndex() ) );
		element.addAttribute("keyword", String.valueOf( inDetail.isKeyword() ) );
		element.addAttribute("filter", String.valueOf( inDetail.isFilter() ) );
		element.addAttribute("stored", String.valueOf( inDetail.isStored() ) );
		element.addAttribute("editable", String.valueOf( inDetail.isEditable() ) );
		element.addAttribute("sortable", String.valueOf( inDetail.isSortable() ) );

		String type = inDetail.getDataType();
		if (type != null)
		{
			element.addAttribute("type", type);
		}

		String viewtype = inDetail.getViewType();
		if (viewtype != null)
		{
			element.addAttribute("viewtype", viewtype);
		}

		for (Iterator iterator = inDetail.getElementData().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String val = (String) inDetail.get(key);
			if(!"name".equals(key)){
				element.addAttribute(key, val);
			}
		}
	}

	protected void populateViewElements(Element inElement, PropertyDetail inDetail)
	{

		String label = inElement.getTextTrim();
		if (label != null && label.length() > 0)
		{
			inDetail.setName(label);
		} 
		
		else{
			//Element nameinfo = inElement.element("name");
			//Override this later...to support overriding names in other languages.
		
			
		}
		
		

		// Set all the remaining attributes as properties
		for (Iterator iterator = inElement.attributeIterator(); iterator.hasNext();)
		{
			Attribute attr = (Attribute) iterator.next();
			String name = attr.getName();
			String value = attr.getValue();
			inDetail.setValue(name, value);
			// log.info("Read" + name + " " + value);
		}

	}

	protected PropertyDetail createDetail(Map defaults, Element element, String inType)
	{
		PropertyDetail d = new PropertyDetail();
		d.setElementData(new ElementData(element));
		d.setTextLabelManager(getTextLabelManager());
//		for (Iterator iterator = defaults.keySet().iterator(); iterator.hasNext();)
//		{
//			String key = (String) iterator.next();
//			String value = (String) defaults.get(key);
//			d.setValue(key, value);
//		}
		String type = element.attributeValue("type");
		d.setDataType(type);
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

	protected List listFilesByFolderType(String inFolderType, boolean includeExtensions)
	{
		// lists, views, fields
		List datapaths = getPageManager().getChildrenPaths("/WEB-INF/data/" + getCatalogId() + "/" + inFolderType);
		String inPath = "/" + getCatalogId() + "/data/" + inFolderType + "/";
		List basepaths = getPageManager().getChildrenPaths(inPath, true);

		Set set = new HashSet();
		for (Iterator iterator = basepaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			path = PathUtilities.extractPageName(path);
			if (!path.startsWith("_"))
			{
				set.add(path);
			}
		}
		for (Iterator iterator = datapaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			path = PathUtilities.extractPageName(path);
			if (!path.startsWith("_"))
			{
				set.add(path);
			}
		}
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

	public List listSearchTypes()
	{
		List fields = listFilesByFolderType("fields", true);
		HashSet all = new HashSet(fields);
		List lists = listFilesByFolderType("lists", true);
		all.addAll(lists);
		List sorted = new ArrayList(all);
		Collections.sort(sorted);
		return sorted;
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
		return listFilesByFolderType("views/", true);
	}

	public List listViews(String inViewType)
	{
		return listFilesByFolderType("views/" + inViewType, true);
	}

	public Data getView(String inType, String inView)
	{
		// Guess the type is not used. the inview has the type in the path name?
		return getViewXml(inView);
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
					PropertyDetail local = detail.copy();
					local.setView(inViewName);
					populateViewElements(elem, local);
					view.add(local);
				}
			}
		}
		return view;
	}

	public void saveView(String inCatalogId, View inView, User inUser)
	{
		XmlFile file = getXmlArchive().getXml("/WEB-INF/data/" + inCatalogId + "/views/" + inView.getId() + ".xml");
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
		String path = getConfigurationPath("/fields/" + inSearchType + ".xml");
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

		String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inSearchtype + ".xml";
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

}
