/*
 * Created on Dec 6, 2005
 */
package com.openedit.modules.translations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.repository.filesystem.StringItem;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.modules.BaseModule;
import com.openedit.page.FileFinder;
import com.openedit.page.Page;
import com.openedit.page.PageProperty;
import com.openedit.page.PageSettings;
import com.openedit.util.PathUtilities;
import com.openedit.util.URLUtilities;
import com.openedit.web.Browser;

public class TranslationModule extends BaseModule
{

	public static final String PROPERTIES_FROM_MARKUP = "properties_from_markup";

	public Translation getTranslations( WebPageRequest inReq ) throws OpenEditException
	{
		Translation trans = new Translation();

		//get the languages
		init( inReq, trans );

		inReq.putPageValue( "pageManager", getPageManager() );
		inReq.putPageValue( "translations", trans );
		return trans;
	}

	protected void init( WebPageRequest inReq, Translation inTrans ) throws OpenEditException
	{
		//#set( $languages = $page.getPageSettings().getProperty("languages") )
		PageProperty prop = inReq.getPage().getPageSettings().getProperty( "languages" );

		if ( prop != null )
		{
			for ( Iterator iter = prop.getValues().keySet().iterator(); iter.hasNext(); )
			{
				String locale = (String) iter.next();
				String name = (String) prop.getValues().get( locale );
				Language lang = new Language();
				lang.setPageManager( getPageManager() );
				if ( locale.length() == 0 )
				{
					lang.setId( "default" );
					lang.setRootDirectory( "" );
				}
				else
				{
					lang.setId( locale );
					lang.setRootDirectory( "/translations/" + locale );
				}
				lang.setName( name );
				inTrans.addLanguage( lang );
			}
			inTrans.sort();
			Language browser = createBrowserLang( inReq );
			inTrans.getLanguages().add( 0, browser );
		}
		//This is for transition for people who do not have languages setup yet or upgrades
		if ( inTrans.getLanguages().size() == 0 )
		{
			Language browser = createBrowserLang( inReq );
			inTrans.getLanguages().add( browser );
			Language lang = new Language();
			lang.setPageManager( getPageManager() );
			lang.setId( "default" );
			lang.setName( "Language: Use Default" );
			lang.setRootDirectory( "" );
			inTrans.addLanguage( lang );
			//TODO: remove this section
			String done = (String) inReq.getSessionValue( "defaultset" );
			if ( done == null )
			{
				inReq.putSessionValue( "sessionlocale", "default" );
				inReq.putSessionValue( "defaultset", "true" );
			}
		}
		String selectedLang = inReq.getLanguage();
		inTrans.setSelectedLang( selectedLang );
	}

	protected Language createBrowserLang( WebPageRequest inReq )
	{
		Language lang = new Language();
		lang.setPageManager( getPageManager() );
		lang.setId( "browser" );
		Browser browser = (Browser) inReq.getPageValue( "browser" );
		if ( browser != null )
		{
			lang.setName( "Language: " + browser.getLocale() );
		}
		lang.setRootDirectory( "" );
		return lang;
	}

	public void changeLanguage( WebPageRequest inReq ) throws Exception
	{
		String newlang = inReq.getRequestParameter( "newlang" );
		getTranslations( inReq );
		if ( newlang != null )
		{
			if ( newlang.equals( "locale_browser" ) )
			{
				inReq.removeSessionValue( "sessionlocale" );
			}
			else
			{
				String locale = newlang.substring( "locale_".length() );
				inReq.putSessionValue( "sessionlocale", locale );
			}
			String orig = inReq.getRequestParameter( "origURL" );
			if ( orig != null )
			{
				inReq.redirect( orig );
			}
		}
	}

	//for editing
	protected Language getEditingLanguage( WebPageRequest inReq )
	{
		String id = (String) inReq.getSessionValue( "editinglanguage" );

		Translation trans = (Translation) inReq.getPageValue( "translations" );

		return trans.getLanguage( id );
	}

	public void selectElement( WebPageRequest inReq ) throws Exception
	{
		String eid = inReq.getRequestParameter( "elementid" );
		if ( eid != null )
		{
			Translation trans = (Translation) inReq.getPageValue( "translations" );
			Language lang = trans.getLanguage( eid );
			inReq.putSessionValue( "editinglanguage", lang.getId() );
		}
	}

	public void deleteElement( WebPageRequest inReq ) throws Exception
	{
		Translation trans = (Translation) inReq.getPageValue( "translations" );
		Language selectedlan = getEditingLanguage( inReq );
		trans.removeLanguage( selectedlan );
		save( trans, inReq );
		inReq.putSessionValue( "editinglanguage", null );
	}

	public void saveElement( WebPageRequest inReq ) throws Exception
	{
		Translation trans = (Translation) inReq.getPageValue( "translations" );
		Language selectedlan = getEditingLanguage( inReq );

		String id = inReq.getRequestParameter( "id" );
		selectedlan.setId( id );
		String text = inReq.getRequestParameter( "nametext" );
		selectedlan.setName( text );

		save( trans, inReq );
		inReq.putPageValue( "message", "save complete" );
	}

	protected void save( Translation inTrans, WebPageRequest inReq ) throws OpenEditException
	{
		Page settings = getPageManager().getPage( "/_site.xconf" );

		//depends on the language we are editing in
		String tagname = "languages";
		PageProperty props = new PageProperty( tagname );

		for ( Iterator iter = inTrans.getLanguages().iterator(); iter.hasNext(); )
		{
			Language l = (Language) iter.next();
			if ( !l.getId().equals( "browser" ) )
			{
				if ( l.getId().equals( "default" ) )
				{
					props.setValue( l.getName(), "" );
				}
				else
				{
					props.setValue( l.getName(), l.getId() );
				}
			}
		}
		settings.getPageSettings().getProperties().put( tagname, props );
		getPageManager().getPageSettingsManager().saveSetting( settings.getPageSettings() );
	}

	public void addElement( WebPageRequest inReq ) throws Exception
	{
		Translation trans = (Translation) inReq.getPageValue( "translations" );
		Language lang = new Language();
		lang.setId( "new" );
		lang.setName( "new" );
		trans.addLanguage( lang );
		inReq.putSessionValue( "editinglanguage", lang.getId() );
		save( trans, inReq );
	}

	//this is not used any more
	public void autoTranslate( WebPageRequest inReq ) throws Exception
	{
		createPropertiesFromMarkup( inReq );
		String[] pageTypes = inReq.getRequestParameters( "pagetype" );

		if ( pageTypes == null )
		{
			return;
		}
		Translation trans = (Translation) inReq.getPageValue( "translations" );
		String targetPage = inReq.getRequestParameter( "targetpage" );
		List pages = targetPages( inReq );
		inReq.putPageValue( "foundpages", pages );
		String targetLanguage = inReq.getRequestParameter( "targetlanguage" );
		for ( int i = 0; i < pageTypes.length; i++ )
		{
			String pageType = pageTypes[ i ];

			List allTranslations = new ArrayList();
			for ( Iterator iter = pages.iterator(); iter.hasNext(); )
			{
				Page page = (Page) iter.next();
//				if ( !page.isHtml() )
//				{
//					continue;
//				}
				if ( targetPage != null && targetPage.length() > 0 && page.exists() )
				{
					if ( pageType.equals( "properties" ) )
					{
						PageSettings pageSettings = page.getPageSettings();
						Map properties = pageSettings.getProperties();

						List transList = trans.webTranslateProperties( properties, targetLanguage );
						if ( transList.size() > 0 )
						{
							getPageManager().getPageSettingsManager().saveSetting( pageSettings );
							allTranslations.addAll( transList );
							
						}
					}
					else if ( pageType.equals( "content" ) )
					{
						Language lang = trans.getLanguage( targetLanguage );
						String translatedPath = PathUtilities.createDraftPath( lang
								.getRootDirectory()
								+ page.getPath() );
						Page translatedPage = getPageManager().getPage( translatedPath );
						if ( !translatedPage.exists() )
						{
							String text = page.getContent();
							String translatedText = trans.webTranslate( text, targetLanguage );
							translatedText = URLUtilities.xmlUnescape( translatedText );

							//TODO: We shouldn't assume UTF-8, but who is using anything else for multi-lingual support right now?
							StringItem translatedContent = new StringItem( translatedPath,
									translatedText, "UTF-8" );
							translatedContent.setMessage( "Automatically Translated" );
							translatedContent.setAuthor( inReq.getUser().getUserName() );
							page.setContentItem( translatedContent );
							getPageManager().putPage( page );
							inReq.putPageValue( "transPage", translatedPath );
						}
						else
						{
							inReq.putPageValue( "error",
									"The specified page already exists for that language ("
											+ lang.getName() + ")." );
						}
					}
					else
					{
						inReq
								.putPageValue( "error",
										"You must specify whether you need either the page's properties or content translated." );
					}
				}
				else
				{
					inReq.putPageValue( "error", "The specified page does not exist." );
				}
			}
			inReq.putPageValue( "transList", allTranslations );
		}
	}

	public void createPropertiesFromMarkup( WebPageRequest inRequest )
	{
		String propertiesFromMarkup = inRequest.getRequestParameter( PROPERTIES_FROM_MARKUP );
		if ( propertiesFromMarkup == null || !PROPERTIES_FROM_MARKUP.equals( propertiesFromMarkup ) )
		{
			return;
		}

		List pages = targetPages( inRequest );
		for ( Iterator iterator = pages.iterator(); iterator.hasNext(); )
		{
			Page page = (Page) iterator.next();
			createPropertiesFromPage( inRequest, page );
		}

	}

	protected void createPropertiesFromPage( WebPageRequest inRequest, final Page inPage ) throws OpenEditException
	{
		final PageSettings pageSettings = inPage.getPageSettings();
		TranslationParser parser = new TranslationParser();
		class FlagHolder
		{
			boolean flag;
		}
		final FlagHolder translationOccured = new FlagHolder();
		parser.setListener( new TranslationEventListener()
		{

			public void translationEvent( String key, String value )
			{
				String property = inPage.getProperty(  Translation.PREFIX + key );
				if ( property == null )
				{
					PageProperty pageProperty = new PageProperty( Translation.PREFIX + key );
					pageProperty.setValue( value );
					pageSettings.putProperty( pageProperty );
					translationOccured.flag = true;
				}
			}

			public void tokenEvent( String token )
			{

			}
			
			public void escapeEvent(String expression)
			{
				
			}

		} );
		
		parser.parse( inPage.getContent() );

		if ( translationOccured.flag )
		{
			getPageManager().getPageSettingsManager().saveSetting( pageSettings );
		}
	}

	protected List targetPages( WebPageRequest inReq )
	{
		String targetPage = inReq.getRequestParameter( "targetpage" );
		Page tarpage = getPageManager().getPage( targetPage );
		List pages = null;

		if ( tarpage.isFolder() || targetPage.indexOf( "*" ) > -1 )
		{
			FileFinder finder = new FileFinder();
			finder.setRoot(getRoot());
			String recursive = inReq.getRequestParameter( "includesubs" );
			finder.setRecursive( Boolean.parseBoolean( recursive ) );
			finder.setPageManager( getPageManager() );
			if ( tarpage.isFolder() && targetPage.indexOf( "*" ) == -1 )
			{
				if( !targetPage.endsWith("/") )
				{
					targetPage = targetPage + "/";
				}
				targetPage = targetPage + "*";
			}
			pages = finder.findPages( targetPage );
			if ( pages.size() == 0 )
			{
				inReq.putPageValue( "error", "No matches found " + targetPage );
			}
		}
		else
		{
			pages = new ArrayList( 1 );
			pages.add( tarpage );
		}
		return pages;
	}
	
}
