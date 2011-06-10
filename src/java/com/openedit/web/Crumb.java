/*
 * Created on May 24, 2004
 */
package com.openedit.web;

import java.util.ArrayList;
import java.util.List;


/**
 * @author cburkey
 *
 */
public class Crumb {
	String fieldText;
	String fieldPath;
	Crumb fieldParent;
	boolean fieldFinal;
	public Crumb()
	{
		
	}
	public Crumb getParent() {
		return fieldParent;
	}
	public void setParent(Crumb inParent) 
	{
		//look for myself in the list. If found then pull off old parent
/*		Crumb self = inParent;
		while( self != null && self.getText() != null)
		{
			if( !getText().equals(self.getText() ) )
			{
				inParent = self.getParent();
				break;
			}
			self = self.getParent();
		}
	*/	fieldParent = inParent;
	}
	public String getPath() {
		return fieldPath;
	}
	public void setPath(String inPath) {
		fieldPath = inPath;
	}
	public String getText() {
		return fieldText;
	}
	public void setText(String inText) {
		fieldText = inText;
	}
	String toLink()
	{
		if ( getText() == null )
		{
			return "";
		}
		return "<a href='" + getPath()+ "'>" + getText() + "</a>";
	}
	public List getCrumbs()
	{
		List parents = new ArrayList();
		Crumb parent = this;
		while(parent != null )
		{
			parents.add(0, parent);
			parent = parent.getParent();
		}
		return parents;
	}
	public boolean isFinal() {
		return fieldFinal;
	}
	public void setFinal(boolean inFinal) {
		fieldFinal = inFinal;
	}
	
	public String toString()
	{
		if ( getParent() == null )
		{
			return toLink();
		}
		else
		{
			return getParent().toString() + " : " + toLink();
		}
	}
}
