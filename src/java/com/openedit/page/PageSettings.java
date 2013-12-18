/*
 * Created on Jul 21, 2004
 */
package com.openedit.page;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.collections.set.ListOrderedSet;
import org.openedit.repository.ContentItem;

import com.openedit.OpenEditException;
import com.openedit.config.Configuration;
import com.openedit.page.manage.TextLabelManager;
import com.openedit.util.PathUtilities;

/**
 * This class represents the possible meta data for a Page
 */
public class PageSettings 
{
	protected ContentItem fieldXConf;
	protected Configuration fieldUserDefinedData;
	protected long fieldModifiedTime;
	
	protected String fieldLayout;
	protected String fieldInnerLayout;
	protected List fieldGenerators;
	protected Map fieldProperties;
	protected List fieldPageActions;
	protected List fieldScripts;
	protected List fieldStyles;
	protected List fieldPathActions;
	protected TextLabelManager fieldTextLabels;
	protected String fieldAlternateContentPath;
	protected boolean fieldOriginalyExistedContentPath; //used to see if a new file has need added or removed

	protected List fieldPermissions;
	
	protected boolean fieldModified = false;
	protected String fieldMimeType;
	protected PageSettings fieldFallBack;
	protected PageSettings fieldParent;
	protected String fieldDefaultLanguage;
	
	public String getAlternateContentPath()
	{
			return fieldAlternateContentPath;
	}
	public void setAlternateContentPath( String alternateContentPath )
	{
		fieldAlternateContentPath = alternateContentPath;
	}
	public String getPath()
	{
		return getXConf().getPath();
	}
	public String toString()
	{
		if ( fieldXConf != null)
		{
			return getXConf().getPath();
		}
		return super.toString();
		
	}
	public List getGenerators()
	{
		//add top level parents last
		List finalList = new ArrayList(4);
	
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldGenerators != null)
			{
				finalList.addAll(parent.fieldGenerators);
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldGenerators != null)
					{
						finalList.addAll(chain.fieldGenerators);
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}	
		//For when the fall back tree is deeper than the url tree
		while( fallbackparent != null)
		{
			if( fallbackparent.fieldGenerators != null)
			{
				finalList.addAll(fallbackparent.fieldGenerators);
			}
			fallbackparent = fallbackparent.getFallback();
		}
		
		return finalList;
	}
	public void setGenerators( List generators )
	{
		fieldGenerators = generators;
	}
	public String getLayout()
	{
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldLayout != null) //now check the real parent
			{
				return parent.fieldLayout;
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldLayout != null)
					{
						return chain.fieldLayout;
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent(); //mirror site parent
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}
		return null;
	}
	//Not used 
	public String getInnerLayout()
	{
		return getInnerLayoutExcludeSelf(null);
	}
	public String getInnerLayoutExcludeSelf(String inPath)
	{
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldInnerLayout != null) //now check the real parent
			{
				String fixed = replaceProperty(parent.fieldInnerLayout);
				if( inPath == null || !inPath.equals(fixed) )
				{
					return fixed;//parent.fieldInnerLayout;
				}
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldInnerLayout != null)
					{
						String fixed = replaceProperty(chain.fieldInnerLayout);
						if( inPath == null || !inPath.equals(fixed) )
						{
							return fixed;//fallbackparent.fieldInnerLayout;
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent(); //mirror site parent
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}
		return null;
	}
	
	public void setLayout( String layout )
	{
		fieldLayout = layout;
	}
	public void setInnerLayout( String innerLayout )
	{
		fieldInnerLayout = innerLayout;
	}
	public List getFallBacks()
	{
		List finalList = new ArrayList();
		
		PageSettings fallbackparent = getFallback();
		while( fallbackparent != null)
		{
			finalList.add(fallbackparent);
			fallbackparent = fallbackparent.getFallback();
		}
		return finalList;
	}
	public String getParentFolder()
	{
		return PathUtilities.extractDirectoryName(getPath());
	}
	
	public String getParentPath()
	{
		return PathUtilities.extractDirectoryPath(getPath());
	}
	
	
	public List getPageActions()
	{
		//add top level parents last
		List finalList = new ArrayList();
	
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldPageActions != null)
			{
				finalList.addAll(0,parent.fieldPageActions);
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldPageActions != null)
					{
						finalList.addAll(0,chain.fieldPageActions);
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}			
		return finalList;

	}
	public List getScripts()
	{
		//add top level parents last
		List finalList = new ArrayList();
	
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldScripts != null)
			{
				finalList.addAll(0,parent.fieldScripts);
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldScripts != null)
					{
						finalList.addAll(0,chain.fieldScripts);
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}			
		return finalList;
	}
	public List getStyles()
	{
		//add top level parents last
		List finalList = new ArrayList();
	
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldStyles != null)
			{
				finalList.addAll(0,parent.fieldStyles);
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldStyles != null)
					{
						finalList.addAll(0,chain.fieldStyles);
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}			
		return finalList;

	}

	public void setPageActions( List pageActions )
	{
		fieldPageActions = pageActions;
	}
	public List getPathActions()
	{
		//add top level parents last
		List finalList = new ArrayList(4);
	
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldPathActions != null)
			{
				finalList.addAll(0,parent.fieldPathActions);
			}
			if ( fallbackparent != null) 
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldPathActions != null)
					{
						finalList.addAll(0,chain.fieldPathActions);
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}
			}
			parent = parent.getParent();
		}			
		return  finalList; //oldest on bottom
	}

	protected int getDepth()
	{
		int i = 0;
		PageSettings settings = this;
		while( settings != null)
		{
			i++;
			settings = settings.getParent();
		}
		return i;
	}
	public Map getProperties()
	{
		if ( fieldProperties == null )
		{
			fieldProperties = new HashMap();
		}
		return fieldProperties;
	}
	public List getAllProperties()
	{
		return getAllProperties(true);
	}
	public List getAllProperties(boolean inIncludeParents)
	{
		Set all = ListOrderedSet.decorate(new ArrayList() );
		Set keys = new HashSet();
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldProperties != null)
			{
				for (Iterator iterator = parent.fieldProperties.keySet().iterator(); iterator.hasNext();)
				{
					PageProperty prop = (PageProperty)parent.fieldProperties.get((String)iterator.next());
					if( !keys.contains(prop.getName()))
					{
						all.add(prop);
						keys.add(prop.getName());
					}
				}
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldProperties != null)
					{
						for (Iterator iterator = chain.fieldProperties.keySet().iterator(); iterator.hasNext();)
						{
							PageProperty prop = (PageProperty)chain.fieldProperties.get((String)iterator.next());
							if( !keys.contains(prop.getName()))
							{
								all.add(prop);
								keys.add(prop.getName());
							}
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
			if( !inIncludeParents )
			{
				break;
			}
		}			
		return new ArrayList(all);
		
	}
	
	public List getAllPropertyKeysWithPrefix(String inPrefix)
	{
		List all = new ArrayList();
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldProperties != null)
			{
				for (Iterator iter = parent.fieldProperties.keySet().iterator(); iter.hasNext();)
				{
					String  key = (String ) iter.next();
					if( key.startsWith(inPrefix))
					{
						key = key.substring(inPrefix.length());
						all.add(key);
					}
				}
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldProperties != null)
					{
						for (Iterator iter = chain.fieldProperties.keySet().iterator(); iter.hasNext();)
						{
							String  key = (String) iter.next();
							if( key.startsWith(inPrefix))
							{
								key = key.substring(inPrefix.length());
								all.add(key);
							}
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}			
		return all;
		
	}
	
	public PageProperty getProperty(String inKey)
	{
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( parent.fieldProperties != null)
			{
				PageProperty val = parent.getFieldProperty(inKey);
				if ( val != null)
				{
					return val;
				}
			}
			if ( fallbackparent != null)  //first check the mirror site
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldProperties != null)
					{
						PageProperty val = (PageProperty)chain.getFieldProperty(inKey);
						if ( val != null)
						{
							return val;
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}		
		while( fallbackparent != null)
		{
			PageProperty val = fallbackparent.getFieldProperty(inKey);
			if ( val != null)
			{
				return val;
			}
			fallbackparent = fallbackparent.getFallback();
		}
		return null;

	}
	public PageProperty getFieldProperty(String inKey )
	{
		PageProperty val = (PageProperty)getProperties().get(inKey);
		if (val != null )
		{
			val.setPath(getXConf().getPath());
		}
		return val;
	}
	public void setProperties( Map properties )
	{
		fieldProperties = properties;
	}
	public void putProperty(PageProperty inProperty)
	{
		getProperties().put( inProperty.getName(), inProperty);
	}

	public PageSettings getFallback()
	{
		return fieldFallBack;
	}
	/**
	 * @param inParent
	 */
	public void setFallBack(PageSettings inParent)
	{
		fieldFallBack = inParent;
		
	}

	public boolean isCurrent()
	{
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		while ( parent != null)
		{
			if( !parent.fieldIsCurrent() ) //check the real parent
			{
				return false;
			}
			if ( fallbackparent != null)  
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if(  !chain.fieldIsCurrent() )
					{
						return false;
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent(); //mirror site parent
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}
		return true;
	}
	public boolean fieldIsCurrent()
	{
	//	System.out.println("Checking " + getPath());
		long last = getXConf().getLastModified();
//		if ( last == -1)
//		{
//			return false;
//		}
		boolean self =  last == getModifiedTime();
		return self;
	}
	public boolean exists()
	{
		return getXConf().exists();
	}
	public ContentItem getXConf()
	{
		return fieldXConf;
	}
	public void setXConf(ContentItem inConf)
	{
		fieldXConf = inConf;
		if ( inConf != null )
		{
			setModifiedTime( inConf.getLastModified() );
		}
		else
		{
			setModifiedTime( -1 ); //TODO: Why not have 0 in here?
		}
	}
	/**
	 * @return
	 */
	public Reader getReader() throws OpenEditException
	{
		try
		{
			String enc = getPageCharacterEncoding();
			if ( enc != null)
			{
				return new InputStreamReader(getXConf().getInputStream(), enc );
			}
			return new InputStreamReader(getXConf().getInputStream(), "UTF-8");

		}
		catch ( Exception ex )
		{
			throw new OpenEditException(ex);
		}
	}
	public long getModifiedTime()
	{
		return fieldModifiedTime;
	}
	public void setModifiedTime(long inLastModifiedTime)
	{
		fieldModifiedTime = inLastModifiedTime;
	}
	
	public String getPageCharacterEncoding()
	{
		return getPropertyValue("encoding", null);
	}
	public Configuration getUserDefinedData()
	{
		return fieldUserDefinedData;
	}
	public void setUserDefinedData(Configuration inUserDefinedData)
	{
		fieldUserDefinedData = inUserDefinedData;
	}
	/**
	 * @param inString
	 * @return
	 */
	public Configuration getUserDefined(String inString)
	{
		if ( getUserDefinedData() != null)
		{
			Configuration config =  getUserDefinedData().getChild(inString);
			if ( config != null )
			{
				return config;
			}
		}
		return null;
	}
	/**
	 * @param inList
	 */
	public void setPathActions(List inList)
	{
		fieldPathActions = inList;
	}
	/**
	 * @param inString
	 * @return
	 */
	public String getPropertyValue(String inString, Locale inLocale)
	{
		PageProperty prop = getProperty(inString);
		if ( prop != null)
		{
			return replaceProperty(prop.getValue(inLocale));
		}
		return null;
		
	}
	
	/**
	 * @return Returns the mimeType.
	 */
	public String getMimeType() {
		return fieldMimeType;
	}
	/**
	 * @param inMimeType The mimeType to set.
	 */
	public void setMimeType(String inMimeType) {
		fieldMimeType = inMimeType;
	}
	
	public PageSettings getParent()
	{
		return fieldParent;
	}
	public void setParent(PageSettings inParent)
	{
		fieldParent = inParent;
	}
	public String getFieldAlternativeContentPath()
	{
		return fieldAlternateContentPath;
	}
	public List getFieldGenerator()
	{
		return fieldGenerators;
	}
	public String getFieldInnerLayout()
	{
		return fieldInnerLayout;
	}
	public String getFieldLayout()
	{
		return fieldLayout;
	}
	public String getDefaultLanguage()
	{
		if( fieldDefaultLanguage == null)
		{
			fieldDefaultLanguage = getPropertyValue("defaultlanguage", null);
			if( fieldDefaultLanguage == null)
			{
				fieldDefaultLanguage = "";
			}
		}
		return fieldDefaultLanguage;
	}
	
	public String getPropertyValueFixed( String inKey )
	{
		String val = getPropertyValue(inKey, null);
		return replaceProperty(val);
	}
	public String replaceProperty(String inValue)
	{
		if( inValue == null)
		{
			return inValue;
		}
		int start = 0;
		while( (start = inValue.indexOf("${",start)) != -1)
		{
			int end = inValue.indexOf("}",start);
			if( end != -1)
			{
				String key = inValue.substring(start+2,end);
				Object variable = getProperty(key); //check for property
				
				if( variable != null)
				{
					String sub = variable.toString();
					sub = replaceProperty(sub);
					inValue = inValue.substring(0,start) + sub + inValue.substring(end+1);
					if(sub.length() <= end){
						start = end-sub.length();
					}else{
						start =  sub.length();
					}
				}else{
					start = end;
				}
			}
		
			
		}
		return inValue;
	}
	public boolean isOriginalyExistedContentPath()
	{
		return fieldOriginalyExistedContentPath;
	}
	public void setOriginalyExistedContentPath(boolean inOriginalContentPathMissing)
	{
		fieldOriginalyExistedContentPath = inOriginalContentPathMissing;
	}
	
	public Permission getLocalPermission(String inName)
	{
		if (fieldPermissions == null)
		{
			return null;
		}
		return findPermission(fieldPermissions, inName);
	}
	
	public Permission getPermission(String inName)
	{
		return getPermission(inName,true);
	}
	public Permission getPermission(String inName, boolean includeself)
	{
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		
		if( !includeself)
		{
			parent = parent.getParent();
//			if( fallbackparent != null)
//			{
//				fallbackparent = fallbackparent.getParent();
//				if( fallbackparent == null)
//				{
//					fallbackparent = parent.getFallback();
//				}
//
//			}
		}

		while ( parent != null)
		{
			if( parent.fieldPermissions != null)
			{
				Permission per = findPermission(parent.fieldPermissions,inName);
				if( per != null)
				{
					return per;
				}
			}
			if ( fallbackparent != null) 
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldPermissions != null)
					{
						Permission per = findPermission(chain.fieldPermissions,inName);
						if( per != null)
						{
							return per;
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}			
		return null;
	}
	protected Permission findPermission(List inList,String inName)
	{
		for (Iterator iterator = inList.iterator(); iterator.hasNext();)
		{
			Permission permission = (Permission) iterator.next();
			if( permission.getName().equals(inName))
			{
				return permission;
			}
		}
		return null;
	}
	public List getPermissions()
	{
		return getPermissions(true);
	}
	/**
	 * Load up based on top level permissions being loaded first. If you override a top level one 
	 * It will be loaded out of order but only once
	 * @param includeself
	 * @return
	 */
	public List getPermissions(boolean includeself)
	{
		Map finalList = ListOrderedMap.decorate(new HashMap());
		//Load each page in reverse then flip the entire list
		//Child1 A B C -> Added C B A
		//Parent1 X Y Z -> Added to end Z Y X
		//flip list  C B A Z Y X -> X Y Z A B C This is logical order for someone editing files by hand
		
		PageSettings parent = this;
		PageSettings fallbackparent = getFallback();
		
		if( !includeself)
		{
			parent = parent.getParent();
//			if( fallbackparent != null)
//			{
//				fallbackparent = fallbackparent.getParent();
//				if( fallbackparent == null)
//				{
//					fallbackparent = parent.getFallback();
//				}
//			}
		}
		while ( parent != null)
		{
			if( parent.fieldPermissions != null)
			{
				for (int i = parent.fieldPermissions.size()-1; i >= 0; i--) {
					Permission per = (Permission) parent.fieldPermissions.get(i);

					Permission old = (Permission)finalList.get(per.getName());

					if( old == null )
					{
						finalList.put(per.getName(),per);
					}
					else
					{
						//Move the old value up to this location
						finalList.remove(old.getName());
						finalList.put(old.getName(),old);
					}
				}
			}
			if ( fallbackparent != null) 
			{
				PageSettings chain = fallbackparent;
				int count = 0;
				while( chain != null && count++ < 10)
				{
					if( chain.fieldPermissions != null)
					{
						for (int i = chain.fieldPermissions.size()-1; i >= 0; i--) {
							Permission per = (Permission) chain.fieldPermissions.get(i);
							Permission old = (Permission)finalList.get(per.getName());
							if( old == null )
							{
								finalList.put(per.getName(),per);
							}
							else
							{
								//Move the old value up to this location
								finalList.remove(old.getName());
								finalList.put(old.getName(),old);
							}
						}
					}
					chain = chain.getFallback();
				}
				fallbackparent = fallbackparent.getParent();
				if( fallbackparent == null)
				{
					fallbackparent = parent.getFallback();
				}

			}
			parent = parent.getParent();
		}
		ArrayList all = new ArrayList( finalList.values() );
		Collections.reverse(all);
		return  all; //oldest on bottom
	}
	public List getFieldPermissions()
	{
		return fieldPermissions;
	}
	
	
	public void setPermissions(List inPermissions)
	{
		fieldPermissions = inPermissions;
	}
	public void addPermission(Permission inPermission)
	{
		removePermission(inPermission);
		if( fieldPermissions == null)
		{
			fieldPermissions = new ArrayList();
		}
		if( fieldPermissions.size() > 0 )
		{
			fieldPermissions.add(0,inPermission);
		}
		else
		{
			fieldPermissions.add(inPermission);
		}
		
	}
	
	public void removePermission(Permission inPermission)
	{
		if( fieldPermissions != null)
		{

			for (Iterator iterator = fieldPermissions.iterator(); iterator.hasNext();)
			{
				Permission permission = (Permission) iterator.next();
				if( permission.getName().equals(inPermission.getName()) )
				{
					fieldPermissions.remove(permission);
					break;
				}
			}
		}
	}
	public String getUserDefined(String inElement, String inAttribute)
	{
		Configuration pdata = getUserDefined(inElement);
		String val = null;
		if( pdata != null)
		{
			val = pdata.getAttribute(inAttribute);
		}
		return val;
	}
	public void removeProperty(String inName)
	{
		getProperties().remove(inName);		
	}
	public void setProperty(String inName, String inValue)
	{
		 if( inValue == null)
		{
			removeProperty(inName);
			return;
		}
		PageProperty prop = getFieldProperty(inName);
		if( prop == null)
		{
			prop = new PageProperty(inName);
			prop.setPath(getPath());
			getProperties().put( inName, prop);
		}
		prop.setValue(inValue);
	}

	public List getFieldPathActions()
	{
		return fieldPathActions;
	}
	public List getFieldPageActions()
	{
		return fieldPageActions;
	}
	
	public TextLabelManager getTextLabels()
	{
		return fieldTextLabels;
	}
	public void setTextLabels(TextLabelManager inTextLabels)
	{
		fieldTextLabels = inTextLabels;
	}
	public void setScripts(List inScripts)
	{
		fieldScripts = inScripts;
		
	}
	public void setStyles(List inStyles)
	{
		fieldStyles = inStyles;
	}

	
}