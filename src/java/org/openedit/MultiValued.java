package org.openedit;

import java.util.Collection;
import java.util.regex.Pattern;

public interface MultiValued extends Data
{
	
	public static final Pattern VALUEDELMITER = Pattern.compile("\\s\\|\\s");
	
	public Collection<String> getValues(String inPreference);
	public void setValues(String inKey, Collection<String> inValues);
}
