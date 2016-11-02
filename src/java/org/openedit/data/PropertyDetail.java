/*
 * Created on Sep 13, 2005
 */
package org.openedit.data;

import java.text.DateFormat;
import java.util.Map;
import java.util.Set;

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
				|| isViewType("tageditor") || isViewType("libraries") || getId().equals("category") || getId().equals("keywords");
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
		setValue("viewtype", inViewType);
	}
	
	public String getDataType()
	{
		String datatype = get("datatype"); 
		if(datatype == null){
			datatype=get("type");
		}
		return datatype;
	
	 
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
		
		String locale =  inRequest.getLocale();

		return getName(locale);
		
		
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
	public boolean isStored()
	{
		return getBoolean("stored");
	}
	public void setStored( boolean inStored)
	{
		setValue("keyword", inStored);
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
		
		return getElementData().get(inId);
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
		
		if(getId().endsWith("id") || isList() || isMultiValue() ||  getId().contains("sourcepath") ){
			return false;
		}
		
			
		if(isDataType("date") || isDataType("boolean") || isNumber() ) 
		{
			return false;
		}
		if("not_analyzed".equals(getValue("analyzer"))){
			return false;
		}
		return true;
	}
	
	
	public boolean isSortable(){
		return true;
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
		return getElementData().getValue(inId);
	}

	public void setAutoInclude(boolean inTrue)
	{
		setValue("autoinclude", inTrue);
	}

	public void setValue(String inId, Object inValueOf)
	{
		getElementData().setValue(inId, inValueOf);
		
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


}
