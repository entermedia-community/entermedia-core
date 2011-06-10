package org.openedit.xml;

import java.util.Iterator;
import java.util.List;

public class ElementDataIterator implements Iterator
{
		protected List fieldHits;
		protected int hitCount = 0;
		protected int startOffset = 0;

		public ElementDataIterator(List inHits)
		{
			setHits(inHits);
		}

		public ElementDataIterator()
		{
		}
		public void setStartOffset( int inStart)
		{
			startOffset = inStart;
		}
		public void setHits(List inHits)
		{
			fieldHits = inHits;
		}

		public List getHits()
		{
			return fieldHits;
		}

		public boolean hasNext()
		{
			if (hitCount < getHits().size())
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public Object next()
		{
			return new ElementData(getHits().get(startOffset + hitCount++));
		}

		public void remove()
		{
		}
	
}
