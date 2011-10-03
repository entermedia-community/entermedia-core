/*
 * Created on Sep 13, 2005
 */
package org.openedit.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.openedit.Data;

import com.openedit.WebPageRequest;
import com.openedit.page.manage.TextLabelManager;

public class PropertyDetail implements Data, ViewItem, Comparable
{
	protected String fieldId;
	protected String fieldExternalId;
	protected String fieldExternalType;
	protected String fieldCatalogId;
	protected String fieldView;
	
	protected String fieldText;
	protected boolean fieldIndex; //this can be searched as a Lucene field
	protected boolean fieldIsStored;
	protected boolean fieldEditable = false;
	protected boolean fieldFilter;  //This means export it to a list or something
	
	protected boolean fieldKeyword; //this is added to the Keyword string
	private String fieldDateFormatString;
	protected String fieldDataType; //boolean, long, permission, etc...
	protected DateFormat fieldDateFormat;
	protected Map fieldProperties;
	protected TextLabelManager fieldTextLabelManager;
	
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
		return get("searchtype");
	}

	public void setSearchType(String inSearchType)
	{
		setProperty("searchtype", inSearchType);

	}

	public String getView()
	{
		return fieldView;
	}

	public void setView(String inView)
	{
		fieldView = inView;
	}

	public Map getProperties()
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap();
		}
		return fieldProperties;
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
			return getId();
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
		return get("viewtype");
	}

	public void setViewType(String inViewType)
	{
		setProperty("viewtype", inViewType);
	}
	
	public String getDataType()
	{
		return get("datatype");
	}
	
	public void setDataType(String inDataType)
	{
		setProperty("datatype", inDataType);
	}
	
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	public boolean isIndex()
	{
		return fieldIndex;
	}
	public void setIndex(boolean inIndex)
	{
		fieldIndex = inIndex;
	}
	public DateFormat getDateFormat()
	{
		if (fieldDateFormat == null && getDateFormatString() != null)
		{
			fieldDateFormat = new SimpleDateFormat(getDateFormatString());
		}
		return fieldDateFormat;
	}
	public void setDateFormat(DateFormat inDateFormat)
	{
		fieldDateFormat = inDateFormat;
	}
	public String getText()
	{
		return fieldText;
	}

	/**
	 * Create a translation file here: /catalogid/configuration/_text_es.txt
	 * @param inRequest
	 * @return
	 */
	public String getText( WebPageRequest inRequest )
	{
		String value = null;
		if( getTextLabelManager() != null)
		{
			value = getTextLabelManager().getAutoText("/" + getCatalogId() + "/data/fields/", getText(), inRequest.getLocale());
		}
		if( value == null)
		{
			value = getText();
		}
		return value;
	}
	public void setText(String inText)
	{
		fieldText = inText;
	}
	public boolean isKeyword()
	{
		return fieldKeyword;
	}
	public void setKeyword(boolean inKeyword)
	{
		fieldKeyword = inKeyword;
	}
	public boolean isStored()
	{
		return fieldIsStored;
	}
	public void setStored( boolean inStored)
	{
		fieldIsStored = inStored;
	}
	public boolean isEditable()
	{
		return fieldEditable;
	}
	public void setEditable(boolean inEditable)
	{
		fieldEditable = inEditable;
	}
	public String[] getExternalIds()
	{
		if( fieldExternalId != null)
		{
			return fieldExternalId.split(",");
		}
		return null;
	}
	public String getExternalId()
	{
		return fieldExternalId;
	}
	public void setExternalId(String inExternalId)
	{
		fieldExternalId = inExternalId;
	}
//	public boolean isSearchable()
//	{
//		return fieldIsSearchable;
//	}
//	public void setSearchable(boolean inSearchable)
//	{
//		fieldIsSearchable = inSearchable;
//	}
	
	public boolean isFilter()
	{
		// TODO Auto-generated method stub
		return fieldFilter;
	}
	public void setFilter(boolean inFilter)
	{
		fieldFilter = inFilter;
	}
	public String getExternalType()
	{
		return fieldExternalType;
	}
	public void setExternalType(String inExternalType)
	{
		fieldExternalType = inExternalType;
	}
	public boolean isRequired() 
	{
		return Boolean.parseBoolean(get("required"));
	}
	public void setRequired(boolean required) 
	{
		setProperty("required", Boolean.toString(required));
	}
	public void setDateFormatString(String format) 
	{
		fieldDateFormatString = format;	
	}
	public String getDateFormatString()
	{
		return fieldDateFormatString;
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
		return getId();
	}
	public String get(String inId)
	{		
		if( inId.equals("id"))
		{
			return getId();
		}
		else if ( inId.equals("boolean")
				|| inId.equals("number")
				|| inId.equals("date")
				|| inId.equals("file")
				)
		{
			return String.valueOf(inId.equals(getDataType()));
		}
		else if (inId.equals("list")
				|| inId.equals("html")
				)
		{
			return String.valueOf(inId.equals(getViewType()));
		}
		else if ( inId.equals("text"))
		{
			return getText();
		}
		else if ( inId.equals("externalid"))
		{
			return getExternalId();
		}
		else if ( inId.equals("stored"))
		{
			return String.valueOf(isStored());
		}
		else if ( inId.equals("index"))
		{
			return String.valueOf(isIndex());
		}
		else if ( inId.equals("keyword"))
		{
			return String.valueOf(isKeyword());
		}
		else if ( inId.equals("editable"))
		{
			return String.valueOf(isEditable());
		}
		else if ( inId.equals("filter"))
		{
			return String.valueOf(isFilter());
		}
		else if ( inId.equals("externaltype"))
		{
			return getExternalType();
		}
		else if ( inId.equals("format"))
		{
			return getDateFormatString();
		}
		else if (inId.equals("type"))
		{
			return getDataType();
		}
//		else if (inId.equals("viewtype"))
//		{
//			return getViewType();
//		}

		return (String)getProperties().get(inId);
	}
	public void setProperty(String inId, String inValue)
	{
		if( inId.equals("id"))
		{
			setId(inValue);
		}
		else if ( inId.equals("boolean") && inValue.equalsIgnoreCase("true"))
		{
			setDataType("boolean");
		}
		
		else if ( inId.equals("text"))
		{
			setText(inValue);
		}
		else if ( inId.equals("externalid"))
		{
			setExternalId(inValue);
		}
		else if ( inId.equals("stored"))
		{
			setStored(Boolean.parseBoolean(inValue));
		}
		else if ( inId.equals("index"))
		{
			setIndex(Boolean.parseBoolean(inValue));
		}
		else if ( inId.equals("keyword"))
		{
			setKeyword(Boolean.parseBoolean(inValue));
		}
		else if ( inId.equals("editable"))
		{
			setEditable(Boolean.parseBoolean(inValue));
		}
		else if ( inId.equals("filter"))
		{
			setFilter(Boolean.parseBoolean(inValue));
		}
		else if (( inId.equals("list")
						|| inId.equals("html")) && inValue.equalsIgnoreCase("true")  
				)
		{
			setViewType(inId);
		}
		else if (( inId.equals("number")
						|| inId.equals("date")
						|| inId.equals("file")) && inValue.equalsIgnoreCase("true")
				)
		{
			setDataType(inId);
		}
		else if ( inId.equals("externaltype"))
		{
			setExternalType(inValue);
		}
		else if ( inId.equals("format"))
		{
			setDateFormatString(inValue);
		}
		else if ( inId.equals("type"))
		{
			setDataType(inValue);
		}
//		else if ( inId.equals("viewtype"))
//		{
//			setViewType(inValue);
//		}
		else if (inId.equals("catalogid"))
		{
			setCatalogId(inValue);
		}
		else
		{
			if(inValue == null)
			{
				getProperties().remove(inId);
			}
			else
			{
				getProperties().put(inId, inValue);
			}
		}
	}
	
	public String getName()
	{
		return getText();
	}
	
	public void setName(String inName)
	{
		setText(inName);
	}

	public PropertyDetail copy() 
	{
		PropertyDetail d = new PropertyDetail();
		d.fieldCatalogId = fieldCatalogId;
		d.fieldDateFormat = fieldDateFormat;
		d.fieldDateFormatString = fieldDateFormatString;
		d.fieldEditable = fieldEditable;
		d.fieldExternalId = fieldExternalId;
		d.fieldExternalType = fieldExternalType;
		d.fieldFilter = fieldFilter;
		d.fieldId = fieldId;
		d.fieldIndex = fieldIndex;
		d.fieldIsStored = fieldIsStored;
		d.fieldKeyword = fieldKeyword;
		d.fieldText = fieldText;
		d.fieldDataType = fieldDataType;
		
		d.getProperties().putAll(getProperties());

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
}
