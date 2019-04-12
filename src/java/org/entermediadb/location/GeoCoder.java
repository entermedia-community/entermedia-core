package org.entermediadb.location;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.util.FileUtils;
import org.openedit.util.OutputFiller;

public class GeoCoder
{
	private static final Log log = LogFactory.getLog(GeoCoder.class);
	protected String fieldGoogleKey;
	protected String fieldCatalogId;
	protected ModuleManager fieldModuleManager;
	
	// http://maps.google.com/maps/geo?q=1600+Amphitheatre+Parkway,+Mountain+View,+CA&output=xml&key=abcdefg
	//int delay = 0;

	//Hits for this search

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}
	public String getCatalogId()
	{
		return fieldCatalogId;
	}
	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}
	public String getGoogleKey()
	{
		return fieldGoogleKey;
	}
	public void setGoogleKey(String inGoogleKey)
	{
		fieldGoogleKey = inGoogleKey;
	}
	public Position findFirstPosition(String lookupString)
	{
		Collection<Position> all = getPositions(lookupString);
		if( !all.isEmpty() )
		{
			return all.iterator().next();
		}
		log.error("No positions found for " + lookupString);
		return null;
	}
	public List getPositions(String lookupString)
	{
		ArrayList l = new ArrayList();
		String responseString = null;
		try
		{
			if (lookupString == null)
			{
				return null;
			}
			lookupString = URLEncoder.encode(lookupString, "UTF-8");
//			try
//			{
//				Thread.sleep(delay);
//			}
//			catch (InterruptedException e)
//			{
//				// TODO Auto-generated catch block
//				//e.printStackTrace();
//			}
			
			//https://www.googleapis.com/geolocation/v1/geolocate?key=YOUR_API_KEY
			
			String url = "https://maps.googleapis.com/maps/api/geocode/xml?address=" + lookupString + "&sensor=false";
			if( getGoogleKey() != null)
			{
				url = url + "&key=" + getGoogleKey();
			}

			responseString = downloadToString(url);
			
			Element root1 = null;
			try
			{
				Document document = DocumentHelper.parseText(responseString);
				root1 = document.getRootElement();
			}
			catch (DocumentException e)
			{
				throw new OpenEditException(e);

			}
			Element result = root1.element("result");
			if( result == null)
			{
				return l;
			}

			//TODO: Make a loop?
			
			Element geo = result.element("geometry");
			Element location = geo.element("location");
			Element latelem = location.element("lat");
			Element lngelem = location.element("lng");

			Double lat = Double.parseDouble(latelem.getText());
			Double longi = Double.parseDouble(lngelem.getText());
			//			Double accuracy = Double.parseDouble(data[1]);

			Position p = new Position(lat, longi);
			//		p.setAccuracy(accuracy);
			
			//rsultformatted_address
			p.setResult(result);
			
			l.add(p);

		}
		catch (Exception e)
		{
			log.error("Could not search",e);
			log.error(responseString);
		}
		return l;
		//List positions = parseGoogleResponse(responseString);
	}


public String downloadToString(String inUrl)
{
	StringWriter out = null;
	InputStream in  = null;
	try
	{
		URL url = new URL(inUrl);
		URLConnection con = url.openConnection();
	    con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

		con.setUseCaches(false);
		con.connect(); 
		
		//*** create new output file
        //*** make a growable storage area to read into 
        out = new StringWriter();
        //*** read in url connection stream into input stream
        in = con.getInputStream();
        //*** fill output stream
        new OutputFiller().fill(new InputStreamReader(in),out);
        return out.toString();
	}
	catch ( Exception ex)
	{
		throw new OpenEditException(ex);
	}
	finally
	{
		//*** close output stream
        FileUtils.safeClose(out);
        //*** close input stream
        FileUtils.safeClose(in);
	}
}


//<kml xmlns="http://earth.google.com/kml/2.0">
//<Response>
//  <name>1600 amphitheatre mountain view ca</name>
//  <Status>
//    <code>200</code>
//    <request>geocode</request>
//  </Status>
//  <Placemark>
//    <address> 
//      1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA
//    </address>
//    <AddressDetails Accuracy="8">
//      <Country>
//        <CountryNameCode>US</CountryNameCode>
//	  <AdministrativeArea>
//          <AdministrativeAreaName>CA</AdministrativeAreaName>
//         <SubAdministrativeArea>
//           <SubAdministrativeAreaName>Santa Clara</SubAdministrativeAreaName>
//           <Locality>
//             <LocalityName>Mountain View</LocalityName>
//  	       <Thoroughfare>
//               <ThoroughfareName>1600 Amphitheatre Pkwy</ThoroughfareName>
//             </Thoroughfare>
//             <PostalCode>
//               <PostalCodeNumber>94043</PostalCodeNumber>
//             </PostalCode>
//           </Locality>
//         </SubAdministrativeArea>
//       </AdministrativeArea>
//     </Country>
//   </AddressDetails>
//   <Point>
//     <coordinates>-122.083739,37.423021,0</coordinates>
//   </Point>
// </Placemark>
//</Response>
//</kml>

}