package org.openedit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GenericsUtil
{

	public static <T> List<T> createList()
	{
		return new ArrayList<T>();
	}
	
	public static <T> Set<T> createSet()
	{
		return new HashSet<T>();
	}
	
	public static <T> Collection<T> createCollection()
	{
		return new ArrayList<T>();
	}
	
	
	public static <K, V> Map<K, V> createMap()
	{
		return new HashMap<K, V>();
	}
}
