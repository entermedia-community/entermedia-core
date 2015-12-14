package org.openedit.modules.translations;

import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.hittracker.HitTracker;
import org.openedit.xml.XmlSearcher;

public class TranslationSearcher extends XmlSearcher
{

	

	public String getEntryForLocale(String inLocale, String inKey, boolean auto)
	{
		if(inKey == null){
			return null;
		}
		HitTracker hits = fieldSearch("id", inKey);
		if (hits.size() > 0)
		{
			Data hit = hits.get(0);
			if(hit.get(inLocale) != null){
				return hit.get(inLocale);
			} else{
				if(auto) {
					return translate(inLocale, inKey, true);
				}
			}
			
			
		}
		if(auto) {
			return translate(inLocale, inKey, true);
		}
		return inKey;
	}
	
	
	public String getEntryForLocale(String inLocale, String inKey)
	{
	return	getEntryForLocale(inLocale, inKey, true);
	}

	

	protected Translation fieldTranslation;

	public Translation getTranslation()
	{
		if (fieldTranslation == null)
		{
			fieldTranslation = new Translation();

		}

		return fieldTranslation;
	}

	public void setTranslation(Translation inTranslation)
	{
		fieldTranslation = inTranslation;
	}

	public String translate(String locale, String value, boolean save)
	{

		// comment this back in once we get web translation working again
		String translation = getTranslation().webTranslate(value, locale);

		if (translation == null)
		{
			return null;
		}
		

		if (save)
		{
			Data entry = (Data) searchById(value);
			if (entry == null)
			{
				entry = createNewData();
				String tostore = getXmlArchive().getXmlUtil().xmlEscape(value);
				entry.setId(tostore);

			}
			
			entry.setProperty(locale, translation);
			entry.setName(value);
			saveData(entry, null);

		}
		return translation;

	}

	public PropertyDetails getDefaultDetails()
	{
		if (fieldDefaultDetails == null)
		{
			//fake one
			PropertyDetails details = new PropertyDetails();
			PropertyDetail id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setText("Id");
			id.setId("id");
			id.setEditable(true);
			details.addDetail(id);

			id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setText("Name");
			id.setId("name");
			id.setEditable(true);
			details.addDetail(id);

			fieldDefaultDetails = details;

		}
		return fieldDefaultDetails;
	}

	
	public HitTracker getLanguages()
	{

		return getSearcherManager().getList(getCatalogId(), "languages");
	}
}
