/*
 * Created on Sep 13, 2005
 */
package org.openedit.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.WebPageRequest;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.page.manage.TextLabelManager;
import org.openedit.xml.ElementData;

public class PropertyDetail implements Data,  ViewItem, Comparable
{
//	protected String fieldId;
//	protected String fieldExternalId;
//	protected String fieldExternalType;
	protected String fieldCatalogId;
	protected String fieldView;
	protected String fieldSearchType;
//	
//	protected boolean fieldIndex; //this can be searched as a Lucene field
//	protected boolean fieldIsStored;
//	protected boolean fieldEditable = false;
//	protected boolean fieldFilter;  //This means export it to a list or something
//	protected boolean fieldSortable = false;
//	
	//protected boolean fieldKeyword; //this is added to the Keyword string
	//private String fieldDateFormatString;
	//protected String fieldDataType; //boolean, long, permission, etc...
	protected DateFormat fieldDateFormat;
	//protected ValuesMap fieldProperties;
	protected TextLabelManager fieldTextLabelManager;
	protected ElementData fieldElementData;
	
	protected String fieldInputFilePath;

	public PropertyDetail getChildDetail(String inKey)
	{
		for (Iterator iterator = getObjectDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			if( detail.getId().equals(inKey))
			{
				return detail;
			}
		}
		return null;
	}
	
	public List getObjectDetails() 
	{
		if( fieldObjectDetails == null)
		{
			fieldObjectDetails = new ArrayList();
			if(isDataType("objectarray") || isDataType("nested"))
			{
				for (Iterator iterator = getElementData().getElement().elementIterator("property"); iterator.hasNext();) 
				{
					Element child = (Element) iterator.next();
					ElementData data = new ElementData(child);
					PropertyDetail detail = new PropertyDetail();
					detail.setElementData(data);
					detail.setCatalogId(getCatalogId());
					fieldObjectDetails.add(detail);
				}
			}
		}
		return fieldObjectDetails;
	}

	public void setObjectDetails(List inObjectDetails)
	{
		fieldObjectDetails = inObjectDetails;
	}

	protected List fieldObjectDetails;
	
	
	
	public String getInputFilePath()
	{
		return fieldInputFilePath;
	}

	public void setInputFilePath(String inInputFilePath)
	{
		fieldInputFilePath = inInputFilePath;
	}
	
	public Collection getValues(String inField)
	{
		Collection values = (Collection)getValue(inField);
		return values;
	}

	public PropertyDetail()
	{
		
	}
	
	public TextLabelManager getTextLabelManager()
	{
		return fieldTextLabelManager;
	}

	public void setTextLabelManager(TextLabelManager inTextLabelManager)
	{
		fieldTextLabelManager = inTextLabelManager;
	}

	public String getSearchType()
	{
	
		
		
		return fieldSearchType;
	}

	public void setSearchType(String inSearchType)
	{
		fieldSearchType = inSearchType;
	}

	public String getView()
	{
		return fieldView;
	}

	public void setView(String inView)
	{
		fieldView = inView;
	}


	
	public boolean isDataType(String inDataType)
	{
		String type = getDataType();
		if( type == null || (!type.equals(inDataType ) && type.endsWith("join")))
		{
			type = getViewType();
		}
		if( type != null && type.equals(inDataType))
		{
			return true;
		}
		return false;
	}
	
	public boolean isViewType(String inViewType)
	{
		return inViewType.equals(getViewType());
	}
	
	public boolean isDate()
	{
		return isDataType("date");
	}
	
	public boolean isList()
	{
		return isDataType("list") || isViewType("list");
	}

	public boolean isMultiValue()
	{
		return isViewType("multiselect") 
				|| isDataType("multi")
				|| isDataType("faceprofilegroup")
				|| isDataType("kwmap")
				|| isViewType("entity") || isViewType("tageditor") || isViewType("libraries") 
				|| getId().equals("category")
				|| getId().equals("category-exact") || getId().equals("keywords");
	}

	public boolean isBoolean()
	{
		return isDataType("boolean") || isViewType("boolean");
	}
	public String getListCatalogId()
	{
		String lid = get("listcatalogid");
		if( lid == null)
		{
			return getCatalogId();
		}
		return lid;
	}
	public String getListId()
	{
		String lid = get("listid");
		if( lid == null)
		{
			lid = getId();
			if( lid.contains("."))
			{
				lid = lid.substring(lid.indexOf(".") + 1, lid.length());
			}
		}
		return lid;
	}

	public void setListId(String inListId) 
	{
		setProperty("listid", inListId);
	}
	public String getQuery() 
	{
		return get("query");
	}

	public void setQuery(String query) 
	{
		setProperty("query", query);
	}

	/**
	 * @deprecated use getDataType
	 */
	public String getType()
	{
		return getDataType();
	}
	
	public String getViewType()
	{
		String viewtype = get("rendertype");
		if(viewtype != null){
			return viewtype;
		}
		return get("viewtype");
	}

	public void setViewType(String inViewType)
	{
		setValue("viewtype", inViewType); //TODO: Move to rendertype
		setValue("rendertype", inViewType); //TODO: Move to rendertype
	}
	
	public String getDataType()
	{
		String datatype = get("datatype"); 
		if(datatype == null){
			datatype=get("type");
		}
		return datatype;
	
	 
	}
	
	
	public void populateViewElements(Element inElement)
	{
		String label = inElement.getTextTrim();
		if (label != null && label.length() > 0)
		{
			setName(label);
		}

		else
		{
			//Element nameinfo = inElement.element("name");
			//Override this later...to support overriding names in other languages.

		}

		// Set all the remaining attributes as properties
		for (Iterator iterator = inElement.attributeIterator(); iterator.hasNext();)
		{
			Attribute attr = (Attribute) iterator.next();
			String name = attr.getName();
			if( !name.equals("id"))
			{
				String value = attr.getValue();
				setValue(name, value);
			}
			
			
			// log.info("Read" + name + " " + value);
		}

	}

	
	
	
	
	
	
	public void setDataType(String inDataType)
	{
		setValue("datatype", inDataType);
	}
	
	public String getId()
	{
		return get("id");
	}
	public void setId(String inId)
	{
		setValue("id", inId);
	}
	public boolean isIndex()
	{
		return getBoolean("index");
	}
	private boolean getBoolean(String inString)
	{
		return getElementData().getBoolean(inString);
	}

	public void setIndex(boolean inIndex)
	{
		setValue("index", inIndex);
	}
	
//	public LanguageMap getLabelText()
//	{
//		return fieldText;
//	}
//	public String getText()
//	{
//		if( fieldText != null)
//		{
//			return fieldText.getDefaultText("en");
//		}
//		return null;
//	}
//
//	/**
//	 * Create a translation file here: /catalogid/configuration/_text_es.txt
//	 * @param inRequest
//	 * @return
//	 */
	public String getText( WebPageRequest inRequest )
	{
		if(Boolean.parseBoolean(inRequest.getPageProperty("auto_translate"))){
			String locale =  inRequest.getLocale();

			return getName(locale);
		}
		
		return getName();
		
		
		
	}
//	public void setText(String inText)
//	{
//		if( fieldText == null)
//		{
//			fieldText = new LanguageMap();
//		}
//		fieldText.setText("en", inText);
//	}
	public boolean isKeyword()
	{
		return getBoolean("keyword");
	}
	public void setKeyword(boolean inKeyword)
	{
		setValue("keyword", inKeyword);
	}
//	public boolean isStored()
//	{
//		return getBoolean("stored");
//	}
	public void setStored( boolean inStored)
	{
		setValue("stored", inStored);
	}
	public boolean isEditable()
	{
		return getBoolean("editable");
	}
	public void setEditable(boolean inEditable)
	{
		setValue("editable", inEditable);
	}
	public String[] getExternalIds()
	{
		String externalid = get("externalid");
		
		if( externalid != null)
		{
			return externalid.split(",");
		}
		return null;
	}
	public String getExternalId()
	{
		return get("externalid");
	}
	public void setExternalId(String inExternalId)
	{
		setValue("externalid", inExternalId);
	}
	
	public boolean isFilter()
	{
		// TODO Auto-generated method stub
		return getBoolean("filter");
	}
	public void setFilter(boolean inFilter)
	{
		setValue("filter", inFilter);
	}
	public String getExternalType()
	{
		return get("externaltype");
	}
	public void setExternalType(String inExternalType)
	{
		setValue("externaltype", inExternalType);
	}
	public boolean isRequired() 
	{
		return getBoolean("required");
	}
	public void setRequired(boolean required) 
	{
		setValue("required", required);
	}
	
	public String getCatalogId(String inDefault)
	{
		if( fieldCatalogId == null)
		{
			return inDefault;
		}
		return getCatalogId();
	}
	
	public String getCatalogId()
	{
		return fieldCatalogId;
	}
	
	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}
	
	public String toString()
	{
		return getName();
	}
	public String get(String inId)
	{		
		Object val = getValue(inId);
		if( val == null)
		{
			return null;
		}
		if( val instanceof String)
		{
			return (String)val;
		}
		else
		{
			return String.valueOf(val);
		}
	}
	public void setProperty(String inId, String inValue)
	{
		setValue(inId, inValue);
	}
	
	public String getName()
	{
		return getElementData().getName();
	}
	
	public void setName(String inName)
	{
		getElementData().getLanguageMap("name").setText("en", inName);
	}

	public PropertyDetail copy() 
	{
		PropertyDetail d = new PropertyDetail();
		ElementData data = getElementData().copy();
		d.setElementData(data);
		d.fieldCatalogId = fieldCatalogId;
		d.fieldSearchType = fieldSearchType;
		d.fieldView = fieldView;
		d.fieldTextLabelManager = fieldTextLabelManager;
		
//		d.setValue("catalogid", getCatalogId());
//			
//		d.setValue("editable", isEditable());
//		d.setValue("externalid",getExternalId());
//		d.setValue("externaltype", getExternalType());
//		d.setValue("filter",  = fieldFilter;
//		d.setValue("id", = fieldId;
//		d.setValue("index", = fieldIndex;
//		d.setValue("stored", = fieldIsStored;
//		d.setValue("keyword", = fieldKeyword;
//		d.setValue("text", = fieldText;
//		d.setValue("datatype", = fieldDataType;
//		d.setValue("sortable", = fieldSortable;
		
	//	d.getProperties().putAll(getProperties());

		return d;
	}

	public boolean hasChildren()
	{
		return false;
	}
	
	public boolean isLeaf()
	{
		return true;
	}

	public String getSourcePath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setSourcePath(String inSourcepath)
	{
		// TODO Auto-generated method stub
		
	}

	public int compareTo(Object inO)
	{
		PropertyDetail detail = (PropertyDetail)inO;
		if( getName() == null)
		{
			return 1;
		}
		if( detail.getName() == null)
		{
			return -1;
		}
		return getName().compareTo(detail.getName());
	}
	public boolean isExternalSort()
	{
		
		if( isDataType("date") || isDataType("boolean") || isNumber() )
		{
			return false;
		}
		return isAnalyzed();
	}
	public boolean isAnalyzed()
	{
		if( getId() == null || getId().endsWith("id") || isList() || isMultiValue() ||  getId().contains("sourcepath") ){

			return false;
		}
		
		if(isDataType("date") || isDataType("boolean") || isNumber() ) 
		{
			return false;
		}
		
		String al = (String)getValue("analyzer");
		if( al != null)
		{
			if( "not_analyzed".equals(al) )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		String indextype = (String)getValue("indextype");
		if( indextype != null)
		{
			if( "not_analyzed".equals(indextype) )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		if( isViewType("tageditor"))
		{
			return true;
		}

		
//		if ("description".equals(detail.getId()))
	
		if (isDataType("objectarray"))
		{
			return false;
		}
		else if (isDataType("nested"))
		{
			return false;
		}

		else if (isDataType("geo_point"))
		{
			return false;
		}
		
		return true;
	}
	
	
	public boolean isSortable()
	{
//		if( !isList() )
//		{
			return true;
//		}
//		String sortable = (String)getValue("stored");
//		return Boolean.parseBoolean(sortable);
	}
	
	
	public ElementData getElementData()
	{
		if (fieldElementData == null)
		{
			fieldElementData = new ElementData();
			
		}

		return fieldElementData;
	}

	public void setElementData(ElementData inElementData)
	{
		fieldElementData = inElementData;
	}

	public void setSortable(boolean inSortable) 
	{
		setValue("sortable",inSortable);
	}
	
	public String getDefaultSort()
	{
		String sort = get("sort");
		if( sort == null)
		{
			sort = "name";
		}
		return sort;
	}
	
	
	public String getSortProperty()
	{
		if( isExternalSort() )
		{
			return getId() + "_sorted";  //For lists only?
		}
		
		return getId();
	}

	public boolean isNumber()
	{
		if (isDataType("double") || isDataType("number") || isDataType("long"))
		{
			return true;
		}
		return false;
	}

	public boolean isMultiLanguage()
	{
		return Boolean.parseBoolean(get("multilanguage"));
	}
	
	public boolean isKeywordMap()
	{
		return isDataType("kwmap");
	}
	
	public String getForeignKeyId()
	{
		return get("foreignkeyid");
	}

	public boolean isAutoInclude()
	{
		Object found = getValue("autoinclude");
		if( found == null)
		{
			return false;
		}
		return Boolean.valueOf( (String)found );
	}
	public Object getValue(String inId)
	{
		if("searchtype".equals(inId)){
			return getSearchType();
		}
		if ( inId.equals("boolean")
				|| inId.equals("number")
				|| inId.equals("date")
				|| inId.equals("file")
				)
		{
			return String.valueOf(inId.equals(getDataType()));
		}

		if(inId.equals("catalogid")){
			return getCatalogId();
		}
		
		else if (inId.equals("list")
				|| inId.equals("html")
				)
		{
			return String.valueOf(inId.equals(getViewType()));
		}
		else if ( inId.equals("text"))
		{
			return getName();
		}
		if( "name".equals(inId))
		{
			return getElementData().getLanguageMap("name");
		}
		Object value = getElementData().getValue(inId);
		if( value == null)
		{
			if( inId.equals("datatype"))
			{
				value = getElementData().getValue("type");
			}
			else if( inId.equals("render") )
			{
				value = getElementData().getValue("rendermask");
			}
			else if( inId.equals("rendertype"))
			{
				value = getElementData().getValue("viewtype");
			}


		}
		return value;
	}

	public void setAutoInclude(boolean inTrue)
	{
		setValue("autoinclude", inTrue);
	}

	public void setValue(String inId, Object inValueOf)
	{
		getElementData().setValue(inId, inValueOf);
		if("rendertype".equals(inId)){
			getElementData().setValue("viewtype", inValueOf);

		}
	}

	public boolean isString()
	{
	return !(isBoolean() || isDate() || isNumber() );
	}

	public String getName(String inLocale) {
		LanguageMap map = getElementData().getLanguageMap("name");
			
		String value = map.getText(inLocale);
		if( value == null && getTextLabelManager() != null)
		{
			String name = getName();
			value = getTextLabelManager().getAutoText("/" + getCatalogId() + "/data/fields/",name, inLocale);
			map.setText(inLocale, value);
		}
		if( value == null)
		{
			value = getName();
		}
		return value;
	}

	

	@Override
	public Map getProperties()
	{
		return getElementData().getProperties();
	}

	@Override
	public void setProperties(Map inProperties)
	{
		getElementData().setProperties(inProperties);
		
	}

	@Override
	public Set keySet()
	{
		return getElementData().keySet();
	}

	public boolean isGeoPoint()
	{
		return "geo_point".equals(getDataType());
	}

	public boolean isBadge() 
	{
		String b = get("isbadge");
		return Boolean.valueOf(b);
	}

	public void setDeleted(boolean inB)
	{
		setValue("deleted", inB);
		
	}
	
	public boolean isDeleted() 
	{
		String b = get("deleted");
		return Boolean.valueOf(b);
	}

	public boolean isHighlight()
	{
		String highlight = get("highlight");
		return Boolean.valueOf(highlight);
	}
	
	
	
	


}
