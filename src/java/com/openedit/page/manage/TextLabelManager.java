package com.openedit.page.manage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.util.LocaleManager;

import com.openedit.OpenEditException;
import com.openedit.modules.translations.Translation;
import com.openedit.page.Page;
import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;

public class TextLabelManager
{
	//each Folder has a set of optional text label files. _text.es.txt 
	//cache the map for each folder by language. Use null one for english
	protected Map fieldFolderPaths;
	protected PageManager fieldPageManager;
	public static String AUTO_TRANSLATE = "auto_translate";
	protected LocaleManager fieldLocaleManager;
	protected Translation fieldTranslator;
	protected Map fieldAutoTranslations;
	private static final Log log = LogFactory.getLog(TextLabelManager.class);
	
	public Map getAutoTranslations()
	{
		if (fieldAutoTranslations == null)
		{
			fieldAutoTranslations = new HashMap();
		}
		return fieldAutoTranslations;
	}

	public void setAutoTranslations(Map inAutoTranslations)
	{
		fieldAutoTranslations = inAutoTranslations;
	}

	public Translation getTranslator()
	{
		return fieldTranslator;
	}

	public void setTranslator(Translation inTranslation)
	{
		fieldTranslator = inTranslation;
	}

	public LocaleManager getLocaleManager()
	{
		return fieldLocaleManager;
	}

	public void setLocaleManager(LocaleManager inLocaleManager)
	{
		fieldLocaleManager = inLocaleManager;
	}

	protected Properties NOT_FOUND = new Properties();

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public Map getFolderPaths()
	{
		if (fieldFolderPaths == null)
		{
			fieldFolderPaths = new HashMap(100);
		}
		return fieldFolderPaths;
	}

	public void setFolderPaths(Map inFolderPaths)
	{
		fieldFolderPaths = inFolderPaths;
	}
	public String getTextFor(String inFolder, String english, String inLocale) 
	{
		if( english == null || english.length() == 0 )
		{
			return english;
		}

		String id = makePath(inFolder, inLocale);
		Properties textfile = (Properties)getFolderPaths().get(id);
		if( textfile == null)
		{
			textfile = loadTextFile(inFolder,id, inLocale);
		}
		if( textfile == NOT_FOUND)
		{
			return null;
		}
		String value = textfile.getProperty(english);
		if ( value == null)
		{
			String lang = getParentLanguage(inFolder, inLocale);
			if( lang != null)
			{
				id = makePath(inFolder, lang);
				Properties textfile2 = loadTextFile(inFolder, id, inLocale);
				if( textfile2 == NOT_FOUND || textfile == textfile2)
				{
					return null;
				}
				value = textfile2.getProperty(english);
			}
		}
		
		return value;
	}

	public String autoTranslate(String inFolder, String inEnglish, String inLocale)
	{
		if( inEnglish != null && autoTranslateEnabled(inFolder,inLocale) )
		{
			String lang = getLocaleManager().getLang(inLocale);
			String translation = getTranslator().webTranslate(inEnglish, lang);
			if (translation == null || translation.equalsIgnoreCase(inEnglish))
			{
				translation = inEnglish;
			}
			log.info(inFolder +  " translated to " + inLocale + " " + inEnglish + " = '" + translation +"'");
			addLabel(inFolder, inEnglish, translation, lang);
			inEnglish = translation;
		}
		return inEnglish;
	}

	protected String makePath(String inFolder, String inLang)
	{
		return inFolder + "/_text_" + inLang + ".txt";
	}

	protected Properties loadTextFile(String inFolder,String id, String inLang) 
	{
		Properties bundle = null;
		Page textfile = getPageManager().getPage(id);
		if( !textfile.exists())
		{
			//go up one level
			String lang = getParentLanguage(inFolder, inLang);
			if( lang != null)
			{
				String upath = makePath(inFolder, lang);
				textfile = getPageManager().getPage(upath);
			}
		}
		if( textfile.exists())
		{
			Reader in = textfile.getReader();
			bundle = new Properties();
			try
			{
				bundle.load(in);
			}
			catch( IOException ex)
			{
				throw new OpenEditException(ex);
			}
			finally
			{
				FileUtils.safeClose(in);
			}
		}
		else
		{
			bundle = NOT_FOUND;
		}
		getFolderPaths().put(id,bundle); //requires restart to install
		return bundle;
	}

	protected String getParentLanguage(String inFolder, String inLang)
	{
		if(inLang == null)
		{
			return null;
		}
		
		int last =  inLang.indexOf("_");
		if( last > -1 )
		{
			String cutlang = inLang.substring(0,last);
			//String path = makePath(inFolder, cutlang);
			return cutlang;
		}
		return null;
	}
	
	public void addLabel(String inFolder,String inEnglish,String inTranslated, String inLang) 
	{
		//load up the resource and add to it then save it and clear all caches
		Page page = getPageManager().getPage(inFolder);
		String id = makePath(page.getContentItem().getPath(), inLang);//page.getContentItem().getPath() + "/_text_" + inLang + ".txt";

		Properties 	props = new Properties();

		Page textfile = getPageManager().getPage(id);
		try
		{
			if( textfile.exists())
			{
				Reader in = textfile.getReader();
				try
				{
					props.load(in);
				}
				finally
				{
					FileUtils.safeClose(in);
				}
				
			}
			props.put(inEnglish, inTranslated);
			OutputStream out = getPageManager().saveToStream(textfile);
			try
			{
				props.store(out, "");
			}
			finally
			{
				FileUtils.safeClose(out);
			}
			getFolderPaths().clear();
		}
		catch( IOException ex)
		{
			throw new OpenEditException(ex);
		}
		
	}

	public boolean hasTranslation(String inFolder, String inKey, String inLocale)
	{
		boolean has = getTextFor(inFolder, inKey, inLocale) != null;
		return has;
	}
	
	protected boolean autoTranslateEnabled(String inFolder, String inLocale)
	{
		Boolean auto = (Boolean)getAutoTranslations().get(inFolder + inLocale);
		if( auto == null)
		{
			Page folder = getPageManager().getPage(inFolder);
			if(!folder.exists() )
			{
				auto = Boolean.FALSE;
			}
			String check = folder.getProperty(AUTO_TRANSLATE + "_" + inLocale);
		
			if( check == null && inLocale != null)
			{
				String lang = getLocaleManager().getLang(inLocale);
				check = folder.getProperty(AUTO_TRANSLATE + "_" + lang);
			}
			if( check == null)
			{
				check = folder.getProperty(AUTO_TRANSLATE);			
			}
			auto = Boolean.valueOf(check);
			getAutoTranslations().put(inFolder + inLocale,auto);
		}
		return auto.booleanValue();
	}
	public String getAutoText(String inPath, String inKey, String inLocale)
	{
		if( inKey == null || inKey.length() == 0 )
		{
			return inKey;
		}

		String folder= PathUtilities.extractDirectoryPath(inPath);
		String text = getTextFor(folder, inKey, inLocale);
		if( text == null)
		{
			Page page = getPageManager().getPage(inPath);
			text = page.getProperty("text." + inKey,inLocale);
		}
		if( text == null)
		{
			 text = autoTranslate(folder, inKey, inLocale);
		}
		return text;
		
	}

	public String getAutoText(Page inPage, String inKey, String inLocale)
	{
		if( inKey == null || inKey.length() == 0 )
		{
			return inKey;
		}
		String folder= PathUtilities.extractDirectoryPath(inPage.getPath());

		String text = getTextFor(folder, inKey, inLocale);
		if( text == null)
		{
			text = inPage.getProperty("text." + inKey,inLocale);
		}
		if( text == null)
		{
			 text = autoTranslate(folder, inKey, inLocale);
		}
		return text;
	}
	
}
