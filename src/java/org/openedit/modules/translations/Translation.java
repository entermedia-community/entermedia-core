/*
 * Created on Dec 6, 2005
 */
package org.openedit.modules.translations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.ModuleManager;
import org.openedit.page.PageProperty;

public class Translation
{
	public static final String PREFIX = "text.";
	protected List fieldLanguages;
	protected String  fieldSelectedLang;
	private static final Log log = LogFactory.getLog(Translation.class);
	protected Translator fieldTranslator;
	
	public Translator getTranslator()
	{
		if( fieldTranslator == null)
		{
			EMediaTranslator emedia = new EMediaTranslator();
			emedia.setModuleManager(getModuleManager());
			fieldTranslator = emedia;
		}
		return fieldTranslator;
	}
	public void setTranslator(Translator inTranslator)
	{
		fieldTranslator = inTranslator;
	}


	protected ModuleManager fieldModuleManager;
	
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}
	public List getLanguages()
	{
		if (fieldLanguages == null)
		{
			fieldLanguages = new ArrayList();
		}
		return fieldLanguages;
	}
	public List getEditLanguages()
	{
		if( getLanguages().size() == 0)
		{
			return Collections.EMPTY_LIST;
		}
		
		List sub = new ArrayList();
		for (Iterator iter = getLanguages().iterator(); iter.hasNext();)
		{
			Language lang = (Language) iter.next();
			if( lang.getRootDirectory().length() > 0)
			{
				sub.add(lang);
			}
		}
		return sub;
	}

	public void setLanguages(List inLanguages)
	{
		fieldLanguages = inLanguages;
	}

	public void addLanguage(Language inLang)
	{
		getLanguages().add(inLang);
	}
	public boolean isSelected(Language inLang)
	{
		if( inLang.getId().equals(getSelectedLang()))
		{
			return true;
		}
		return false;
	}
	public String getSelectedLang()
	{
		return fieldSelectedLang;
	}

	public void setSelectedLang(String inSelectedLocale)
	{
		fieldSelectedLang = inSelectedLocale;
	}

	public void sort()
	{
		Collections.sort(getLanguages(), new Comparator() 
		{
			public int compare(Object inO1, Object inO2)
			{
				Language lang1 = (Language)inO1;
				Language lang2 = (Language)inO2;
				return lang1.getId().compareTo(lang2.getId());
			}
		});
	}

	public Language getLanguage(String inEid)
	{
		for (Iterator iter = getLanguages().iterator(); iter.hasNext();)
		{
			Language lang = (Language) iter.next();
			if( lang.getId().equals(inEid))
			{
				return lang;
			}
		}
		return null;
	}

	public void removeLanguage(Language inSelectedlan)
	{
		getLanguages().remove(inSelectedlan);
		setSelectedLang(null);
	}
	public String webTranslate(String inText, String inSourcelang, String inTargetLang)
	{
		 String translated = getTranslator().webTranslate(inText,inSourcelang,inTargetLang);
		 return translated;
	}
	public List webTranslateProperties(Map inProperties, String inLocale)
	{
		for (Iterator iter = inProperties.keySet().iterator(); iter.hasNext();)
		{
			String name = (String) iter.next();
			PageProperty property = (PageProperty)inProperties.get(name);
			
			if ((name.startsWith(PREFIX) || name.equals("title")) 
					&& property.getValues().get(inLocale) == null)
			{
				String text = property.getValue();
				String translated = webTranslate(text, inLocale);
			
				property.setValue(translated, inLocale);
			}
		}
		 
		return createTranslationList(inProperties, inLocale);
	}
	public String webTranslate(String text, String inLocale)
	{
		return getTranslator().webTranslate(text,"en",inLocale);
	}
	
	
//	This method takes a map of page properties and returns a list containing triples of:
	//1. The name of the property 
	//2. The default version of the property
	//3. The version of the property in the language specified by inLocale
	private List createTranslationList(Map inProperties, String inLocale)
	{
		List transList = new ArrayList();
		for (Iterator iter = inProperties.keySet().iterator(); iter.hasNext();)
		{
			String name = (String) iter.next();
			if (name.startsWith(PREFIX) || name.equals("title"))
			{
				PageProperty property = (PageProperty)inProperties.get(name);
				List propertyTriple = new ArrayList();
				propertyTriple.add(name);
				propertyTriple.add(property.getValue());
				propertyTriple.add(property.getValue(inLocale));
				transList.add(propertyTriple);
			}
		}
		return transList;
	}

}
