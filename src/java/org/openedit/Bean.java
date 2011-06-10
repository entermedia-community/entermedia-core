package org.openedit;

/**
 * @author 	Todd Fisher\Christopher Burkey
 * Company: Open Edit
 * Date: 	6/23/2005
 * Notes:	N/A
 */
public class Bean {
	Class fieldBeanDefinition;
	String fieldVersion;
	
	/**
	 * @return Returns the beanDefinition.
	 */
	public Class getBeanDefinition() {
		return fieldBeanDefinition;
	}
	
	/**
	 * @param inBeanDefinition The beanDefinition to set.
	 */
	public void setBeanDefinition(Class inBeanDefinition) {
		fieldBeanDefinition = inBeanDefinition;
	}
	
	/**
	 * @return Returns the link to the javadoc for the specific class
	 * Replaces the periods in the class definition with forward slashes
	 */
	public String asLink()
	{
		String link = getBeanDefinition().getName().replace('.','/');
		return link;
	}

	/**
	 * @return Returns the Friendly Class name
	 */
	public String asClass()
	{
		String className = getBeanDefinition().getName();
		return className;
	}
	
	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return fieldVersion;
	}
	/**
	 * @param inVersion The version to set.
	 */
	public void setVersion(String inVersion) {
		fieldVersion = inVersion;
	}
}
