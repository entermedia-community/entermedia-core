package org.openedit;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.openedit.page.Page;

public class PlugIn implements Comparable
{
	protected List fieldApplications;
	protected String fieldPlugInPath;
	protected URL fieldPluginXml;
	protected boolean fieldInstalled;
	
	public boolean isInstalled()
	{
		return fieldInstalled;
	}
	public void setInstalled(boolean inInstalled)
	{
		fieldInstalled = inInstalled;
	}
	public URL getPluginXml()
	{
		return fieldPluginXml;
	}
	public void setPluginXml(URL inPluginXml)
	{
		fieldPluginXml = inPluginXml;
	}
	protected String fieldId;
	protected Page fieldBasePath;
	protected List fieldDependsOn;
	protected String fieldVersion;
	protected String fieldAvailableVersion;
	protected String fieldAvailableVersionNotes;
	protected String fieldInstallScript;
	protected String fieldBeanName;
	protected String fieldTitle;
	protected String fieldLongDescription;
	protected List fieldDependantPlugins;
	public List getDependantPlugins() {
	if (fieldDependantPlugins == null) {
		fieldDependantPlugins = new ArrayList();
		
	}

	return fieldDependantPlugins;
	}
	public void setDependantPlugins(List dependantPlugins) {
		fieldDependantPlugins = dependantPlugins;
	}
	public void setDependsOn(List dependsOn) {
		fieldDependsOn = dependsOn;
	}
	protected String fieldVendorLink;
	
	public List getApplications()
	{
		if (fieldApplications == null)
		{
			fieldApplications = new ArrayList();
		}
		return fieldApplications;
	}
	public void setApplications(List inApplications)
	{
		fieldApplications = inApplications;
	}
	public String getPlugInPath()
	{
		return fieldPlugInPath;
	}
	public void setPlugInPath(String inPlugInPath)
	{
		fieldPlugInPath = inPlugInPath;
	}
	public List getDependsOn()
	{
		if (fieldDependsOn == null)
		{
			fieldDependsOn = new ArrayList();
		}
		return fieldDependsOn;
	}
	public void addDependsOn(PlugIn inProject)
	{
		if( inProject != null && !getDependsOn().contains(inProject))
		{
			getDependsOn().add(inProject);
			inProject.addDependant(this);
		}
		
		
	}
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	
	public Set getAllDepends()
	{
		Set deps = new HashSet();
		deps.addAll(getDependsOn());
		
		for (Iterator iterator = getDependsOn().iterator(); iterator.hasNext();)
		{
			PlugIn depend = (PlugIn) iterator.next();
			deps.addAll(depend.getAllDepends());
		}
		return deps;
	}
	
	public boolean equals(PlugIn inPlugIn)
	{
		return getId().equals(inPlugIn.getId());
	}
	
	public boolean dependsOn(String inId)
	{
		for (Iterator iterator = getDependsOn().iterator(); iterator.hasNext();)
		{
			PlugIn depend = (PlugIn) iterator.next();
			if( depend.getId().equals(inId) || depend.dependsOn(inId))
			{
				return true;
			}
		}
		return false;
	}
	public void addApplication(Page inApp)
	{
		getApplications().add(inApp);
	}
	public Page getBasePath()
	{
		return fieldBasePath;
	}
	public void setBasePath(Page inBasePath)
	{
		fieldBasePath = inBasePath;
	}
	public String showThumb()
	{
		if( null == getBasePath())
		{
			return "/openedit/images/toolbar/plugin.gif";
		}
		else
		{
			String folder = getBasePath().getName();
			return "/" + folder + "/.oepluginthumb.gif";
		}
	}
	public String getTitle()
	{
		return getTitle(null);
	}
	public String getTitle(String inLocale)
	{
		if( fieldTitle != null)
		{
			return fieldTitle;
		}
		String prop = null;
		if( getBasePath() != null )
		{
			prop = getBasePath().getProperty("oeplugintitle", inLocale);
		}
		if( prop == null)
		{
			prop = getId();
		}
		return prop;
	}
	
	public void setTitle(String inTitle)
	{
		fieldTitle = inTitle;
	}
	
	public String getVersion()
	{
		return fieldVersion;
	}
	public void setVersion(String inVersion)
	{
		fieldVersion = inVersion;
	}
	public String getBeanName()
	{
		String prop = null;
		if( getBasePath() != null)
		{
			prop = getBasePath().getProperty("oepluginbean");
		}
		return prop;
	}
	public String getAvailableVersion()
	{
		return fieldAvailableVersion;
	}
	public void setAvailableVersion(String inAvailableVersion)
	{
		fieldAvailableVersion = inAvailableVersion;
	}
	public String getInstallScript()
	{
		return fieldInstallScript;
	}
	public void setInstallScript(String inUpgradePath)
	{
		fieldInstallScript = inUpgradePath;
	}
	public String getAvailableVersionNotes()
	{
		return fieldAvailableVersionNotes;
	}
	public void setAvailableVersionNotes(String inAvailableVersionNotes)
	{
		if( inAvailableVersionNotes != null && inAvailableVersionNotes.length() == 0)
		{
			fieldAvailableVersionNotes = null;
		}
		else
		{
			fieldAvailableVersionNotes = inAvailableVersionNotes;
		}
	}
	public String getVendorLink()
	{
		return fieldVendorLink;
	}
	public void setVendorLink(String inVendorLink)
	{
		fieldVendorLink = inVendorLink;
	}
	public String getLongDescription()
	{
		return fieldLongDescription;
	}
	public void setLongDescription(String inLongDescription)
	{
		fieldLongDescription = inLongDescription;
	}
	public int compareTo(Object inO)
	{
		PlugIn in = (PlugIn)inO;
		String title = getTitle();
		String otitle = in.getTitle();
		if( title != null && otitle != null)
		{
			return title.toLowerCase().compareTo(otitle.toLowerCase());
		}
		return 0;
	}
	public String toString(){
		return getId();
	}
	public void addDependant(PlugIn edit) 
	{
		getDependantPlugins().add(edit);
		
	}
//	public boolean dependsOn(PlugIn plugin2) {
//		return this.dependson
//	}
//	
	
}
