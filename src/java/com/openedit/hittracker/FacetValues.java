package com.openedit.hittracker;

import java.util.ArrayList;
import java.util.List;

public class FacetValues
{

	
	
	protected List <SelectedFacet> fieldSelectedFacets;

	public List<SelectedFacet> getSelectedFacets()
	{
	if (fieldSelectedFacets == null)
	{
		fieldSelectedFacets = new ArrayList();
		
	}

	return fieldSelectedFacets;
	}

	public void setSelectedFacets(List<SelectedFacet> inSelectedFacets)
	{
		fieldSelectedFacets = inSelectedFacets;
	}

	public void addFacet(SelectedFacet inFacet)
	{
		getSelectedFacets().add(inFacet);
		
	}
	

	
}
