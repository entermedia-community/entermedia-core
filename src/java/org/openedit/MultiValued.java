package org.openedit;

import java.util.Collection;

public interface MultiValued extends Data
{
	public Collection<String> getValues(String inPreference);
	public void setValues(String inKey, Collection<String> inValues);
}
