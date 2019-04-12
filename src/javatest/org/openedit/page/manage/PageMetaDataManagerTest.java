/*
 * Created on Aug 11, 2004
 */
package org.openedit.page.manage;

import org.openedit.BaseTestCase;
import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;
import org.openedit.page.manage.PageSettingsManager;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageMetaDataManagerTest extends BaseTestCase
{

	public PageMetaDataManagerTest( String name )
	{
		super( name );
	}
	
	public PageSettingsManager getConfigurator()
	{
		return getFixture().getPageManager().getPageSettingsManager();
	}
/*	public void testLoadGenerator() throws Exception
	{
	
		PageSettings pageConfig = new PageSettings( );
		getConfigurator().getXconfReader().loadGenerator( pageConfig, getSimpleGeneratorConfig(), null );
		assertNotNull( pageConfig.getGenerator() );
		assertTrue( pageConfig.getGenerator() instanceof VelocityGenerator );
		
		getConfigurator().getXconfReader().loadGenerator( pageConfig, getNestedGeneratorConfig(), null );
		assertNotNull( pageConfig.getGenerator() );
		assertTrue( pageConfig.getGenerator() instanceof NestedGenerator );
	}*/
	
	
	protected Configuration getSimpleGeneratorConfig() 
	{
		XMLConfiguration config = new XMLConfiguration();
		config.setName("generator");		
		config.setAttribute("name","velocity");
		return config;
	}

	protected Configuration getNestedGeneratorConfig()
	{
		/*
		Element generatorElement = getSimpleGeneratorConfig();
		Element innerElement = DocumentHelper.createElement("generator");
		Attribute nameAttribute = DocumentHelper.createAttribute( generatorElement,"name", "jsp" );
		generatorElement.add( nameAttribute );
		generatorElement.add( innerElement );	
		return generatorElement;
		*/
		XMLConfiguration config = new XMLConfiguration("generator");
		config.setAttribute("name","velocity");
		
		Configuration child = config.addChild("generator");
		child.setAttribute("name","jsp");
		
		return config;

		
	}
}
