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
import org.openedit.Data;
import org.openedit.profile.UserProfile;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.page.manage.PageManager;
import com.openedit.page.manage.TextLabelManager;
import com.openedit.users.User;
import com.openedit.util.PathUtilities;

public class PropertyDetailsArchive 
{
	private static final Log log = LogFactory.getLog(PropertyDetailsArchive.class);
	protected XmlArchive fieldXmlArchive;
	protected String fieldCatalogId;
	protected Map fieldPropertyDetails;
	protected Map fieldViewLabels;
	protected TextLabelManager fieldTextLabelManager;
	protected PageManager fieldPageManager;
	
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

	
	public List getDataProperties(String inType) {
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null) {
			return Collections.EMPTY_LIST;
		}
		return details.getDetails();
	}

	public List getIndexProperties(String inType) {
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null) {
			return Collections.EMPTY_LIST;
		}
		return details.findIndexProperties();
	}

	public List getStoredProperties(String inType) {
		PropertyDetails details = getPropertyDetails(inType);
		if (details == null) {
			return Collections.EMPTY_LIST;
		}
		return details.findStoredProperties();
	}

	public String getConfigurationPath(String inPath)
	{
		String path = "/WEB-INF/data/" + getCatalogId() + inPath;
		if( !getPageManager().getRepository().doesExist(path)  )
		{
			path = "/" + getCatalogId() + "/data" + inPath;
			if( !getPageManager().getPage(path).exists() )
			{
				//legacy support
				if( inPath.startsWith("/fields/"))
				{
					inPath = inPath.replace(".xml", "properties.xml");
					inPath = inPath.replace("/fields/", "/");
				}
				path = "/" + getCatalogId() + "/configuration" + inPath;
				
			}
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
		PropertyDetails propdetails = getPropertyDetailsCached(inType);//not cached for now
		if( propdetails == null)
		{
			log.error("No such properties file " + inType);
			return null;
		}
		return getDetails(propdetails, inView, inUser);
	}

	public List getDataProperties(String inType, String inView, UserProfile inProfile) 
	{
		PropertyDetails propdetails = getPropertyDetailsCached(inType);
		if( propdetails == null)
		{
			log.error("No such properties file " + inType);
			return null;
		}
		return getDetails(propdetails, inView, inProfile);
	}
	public boolean viewExists(String inView)
	{
		XmlFile file = getViewXml(inView, null);
		return file.isExist();
	}
	protected XmlFile getViewXml(String inView, String inLevel)
	{
		XmlFile types = null;
		if (inLevel != null) 
		{
			String path = getConfigurationPath(  "/views/" + inView + inLevel + ".xml");
			types = getXmlArchive().getXml(path);
		}
		if (types == null || !types.isExist())
		{
			String path = getConfigurationPath(  "/views/" + inView  + ".xml");
			types = getXmlArchive().getXml( path);
		}
		return types;
	}
	public View getDetails(PropertyDetails propdetails, String inView, User inUser) 
	{
		return getDetails(propdetails, inView, (UserProfile)null);
	}
	
	public View getDetails(PropertyDetails propdetails, String inView, UserProfile inProfile) 
	{
		XmlFile types = getViewXml(inView, null);
		if (types != null && types.isExist())
		{
			View view = readViewElement(propdetails, types.getRoot(), inView);
			if( view != null)
			{
				view.setViewFile(types);
			}
			if (inProfile != null)
			{
				//Allows us to keep all data in the /WEB-INF/data dir
				String propId = "view_" + inView.replace('/', '_');
				Collection values = inProfile.getValues(propId);
				
				if( values != null)
				{
					//remove any that are not in the user values
					List existing = new ArrayList(view);
					for (Iterator iterator = existing.iterator(); iterator.hasNext();)
					{
						PropertyDetail detail = (PropertyDetail) iterator.next();
						if( !values.contains(detail))
						{
							view.remove(detail);
						}
					}
					
					//add new columns
					for (Iterator iterator = values.iterator(); iterator.hasNext();)
					{
						String id = (String) iterator.next();
						if( id.length() > 0 && view.findDetail(id) == null)
						{
							PropertyDetail detail = getDetail(propdetails, inView, id,  null);
							if(detail == null){
												detail = propdetails.getDetail(id);
								
							}
							if(detail != null){
							view.add(detail);
							}
						}
					}
				}
			}
	
			return view;
		}

		return null;
	}
	
	public PropertyDetail getDetail(PropertyDetails propdetails, String inView, String inFieldName, User inUser) 
	{
		String level = null;//
		if (inUser != null) {
			level = (String) inUser.getProperty("datalevel");
		}
		XmlFile types = null;
		if (level != null) 
		{
			String path = getConfigurationPath(  "/views/" + inView + level + ".xml");
			types = getXmlArchive().getXml(path);
		}
		if (types == null || !types.isExist()) {
			String path = getConfigurationPath( "/views/" + inView  + ".xml" );
			types = getXmlArchive().getXml( path);
		}
		if (types != null && types.isExist()) 
		{
			PropertyDetail detail = propdetails.getDetail(inFieldName);
			if (detail != null) {
				Element child = types.getElementById(inFieldName);
				if( child != null)
				{
					PropertyDetail local = detail.copy();
					local.setView(inView);
					local.setCatalogId(getCatalogId());
					//local.setSearchType(getS)
					populateViewElements(child, local);
					return local;
				}
			}
		}
		return null;
	}

	public XmlArchive getXmlArchive() {
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive) {
		fieldXmlArchive = inXmlArchive;
	}

	protected Map getPropertyDetails() {
		if (fieldPropertyDetails == null) {
			fieldPropertyDetails = new HashMap();
		}

		return fieldPropertyDetails;
	}

	/**
	 * This returns the properties found in
	 * store/configuration/{inType}.xml. In other words, all the
	 * possible properties for that type.
	 * 
	 * @param The
	 *            type of data (i.e., product, catalog, item, order, etc...)
	 * @return PropertyDetails
	 */
	public PropertyDetails getPropertyDetails(String inType)
	{
		PropertyDetails details = 	(PropertyDetails) getPropertyDetails().get(inType);
		try
		{
			String path = getConfigurationPath( "/fields/"+ inType + ".xml");
			
			XmlFile settings = getXmlArchive().loadXmlFile(path); //checks time stamp. returns null if changed
			if( details != null && details.getInputFile() == settings)
			{
				return details;
			}
			log.info("Loading " + getCatalogId() + " " +  inType);
			settings = getXmlArchive().getXml(path);
			if (!settings.isExist())
			{
				if( inType.endsWith("Log"))
				{
					path = getConfigurationPath("/fields/defaultLog.xml");
				}
				else
				{
					path = getConfigurationPath( "/fields/default.xml" );
				}
				//This should not happen as well
				settings = getXmlArchive().getXml(path);
			}
			details = new PropertyDetails();
			if( settings.isExist())
			{
				setAllDetails(details,inType,settings.getRoot());
				getViewLabels().clear();
			}
			details.setInputFile(settings);
			getPropertyDetails().put(inType, details);
			return details;
		} catch (OpenEditException ex) {
			throw new OpenEditRuntimeException(ex);
		}
	}

	public PropertyDetails getPropertyDetailsCached(String inType) {
		PropertyDetails details = (PropertyDetails) getPropertyDetails().get(
				inType);
		if (details == null)
		{
			return getPropertyDetails(inType);
		}
		return details;
	}

	public void savePropertyDetails(PropertyDetails inDetails, String inType,
			User inUser) {
		XmlFile file = new XmlFile();
		String path = "/WEB-INF/data/" + getCatalogId() + "/fields/" + inType + ".xml";
		file.setPath(path);
		Element root = DocumentHelper.createElement("properties");
		file.setRoot(root);
		file.setElementName("property");

		for (Iterator iterator = inDetails.getDetails().iterator(); iterator
				.hasNext();) {
			PropertyDetail detail = (PropertyDetail) iterator.next();
			Element element = file.addNewElement();
			fillElement(inDetails.getDefaults(),element, detail);
		}

		getXmlArchive().saveXml(file, inUser);
	}

	public String getCatalogId() {
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId) 
	{
		fieldCatalogId = inCatalogId;
	}

	public PropertyDetail getDataProperty(String inType, String inView, String inField,
			User inUser) {
		PropertyDetails details = getPropertyDetailsCached(inType);
		if( details == null )
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
		//Collections.sort(newdetails);
		details.setDetails(newdetails);
	}

	
	public void fillElement(Map defaults, Element element, PropertyDetail inDetail) {
		element.addAttribute("id", inDetail.getId());
		element.addAttribute("externalid", inDetail.getExternalId());
		element.addAttribute("externaltype", inDetail.getExternalType());
		element.addAttribute("catalogid", inDetail.getCatalogId());

		if (inDetail.getText() != null)
		{
			element.setText(inDetail.getText());
		}

		if (inDetail.isIndex())
			element.addAttribute("index", "true");
		if (inDetail.isKeyword())
			element.addAttribute("keyword", "true");
		if (inDetail.isFilter())
			element.addAttribute("filter", "true");
		if (inDetail.isStored())
			element.addAttribute("stored", "true");
		if (inDetail.isEditable())
			element.addAttribute("editable", "true");

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

		for (Iterator iterator = inDetail.getProperties().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String val = (String)inDetail.getProperties().get(key);
			if(!val.equals(defaults.get(key))){
				element.addAttribute(key, val);
			}
		}
	}

	protected void populateViewElements(Element inElement, PropertyDetail inDetail) {

		String label = inElement.getTextTrim();
		if (label != null &&label.length()>0) 
		{
			inDetail.setText(label);
		}
		
		//Set all the remaining attributes as properties
		for (Iterator iterator = inElement.attributeIterator(); iterator.hasNext();)
		{
			Attribute attr = (Attribute) iterator.next();
			String name = attr.getName();
			String value = attr.getValue();
			inDetail.setProperty(name, value);
			//log.info("Read" + name + " " +  value);
		}

	}
	
	protected PropertyDetail createDetail(Map defaults, Element element, String inType) 
	{
		PropertyDetail d = new PropertyDetail();
		d.setTextLabelManager(getTextLabelManager());
		for (Iterator iterator = defaults.keySet().iterator(); iterator.hasNext();)
		{
			String	key = (String) iterator.next();
			String value = (String) defaults.get(key);
			d.setProperty(key, value);
		}
		String type = element.attributeValue("type");

		d.setDataType(type);
		
		populateViewElements(element, d);

		if (d.getCatalogId() == null)
		{
			d.setCatalogId(fieldCatalogId);
		}
		if (d.getSearchType() == null)
		{
			d.setSearchType(inType);
		}

		
		if( d.isViewType("list") && d.getListId() == null)
		{
			d.setListId(d.getId());
		}
		return d;
	}

	protected List listFilesByFolderType(String inFolderType)
	{
		//lists, views, fields
		List datapaths = getPageManager().getChildrenPaths("/WEB-INF/data/" + getCatalogId() +"/" + inFolderType );
		String inPath = "/" + getCatalogId() +"/data/" + inFolderType +"/";
		List basepaths = getPageManager().getChildrenPaths(inPath ,true);
		Set set = new HashSet();
		for (Iterator iterator = basepaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			path = PathUtilities.extractPageName(path);
			if( !path.startsWith("_") )
			{
				set.add(path);				
			}
		}
		for (Iterator iterator = datapaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			path = PathUtilities.extractPageName(path);
			if( !path.startsWith("_") )
			{
				set.add(path);				
			}
		}
		List sorted = new ArrayList(set);
		Collections.sort(sorted);
		return sorted;
		
	}
	
	public List listSearchTypes()
	{
		List fields =  listFilesByFolderType("fields");
		HashSet all = new HashSet(fields);
		List lists =  listFilesByFolderType("lists");
		all.addAll(lists);
		List sorted = new ArrayList(all);
		Collections.sort(sorted);
		return sorted;
	}

	public List listViewTypes()
	{
		return listFilesByFolderType("views/");
	}

	public List listViews(String inViewType)
	{
		return listFilesByFolderType("views/" + inViewType);
	}

	public Data getView(String inType, String inView)
	{
		return getViewXml(inView,null);
	}
	public String getViewLabel(String inView)
	{
		String usage = (String)getViewLabels().get(inView);
		if( usage == null)
		{
			Data view = getViewXml(inView,null);
			usage = view.get("usagelabel");
			if( usage == null)
			{
				usage = "";
			}
			getViewLabels().put(inView, usage);
		}
		return usage;
	}
	
	protected Map getViewLabels() 
	{
		if( fieldViewLabels == null)
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
		XmlFile file = getXmlArchive().getXml("/WEB-INF/data/" +inCatalogId + "/views/" + inView.getId()  + ".xml");
		file.clear();

		Element root = file.getRoot();
		appendValues(root, inView);
		getXmlArchive().saveXml(file, inUser);
	}

	protected void appendValues(Element inRoot, View inView)
	{
		for (Iterator iterator = inView.iterator(); iterator.hasNext();)
		{
			Object object = (Object) iterator.next();
			if( object instanceof PropertyDetail)
			{
				Element child = inRoot.addElement("property");
				PropertyDetail prop = (PropertyDetail)object;
				child.addAttribute("id", prop.getId() );
			}
			else if( object instanceof View)
			{
				Element child = inRoot.addElement("section");
				View view = (View)object;
				child.addAttribute("title", view.getTitle() );
				appendValues(child,view);
			}
			
		}
		
		
	}
	
}
