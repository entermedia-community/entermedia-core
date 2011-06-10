package org.openedit.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseDataIterator implements Iterator
{
		protected List fieldHits;
		protected int hitCount = 0;
		protected int startOffset = 0;

		public BaseDataIterator(List inHits)
		{
			setHits(inHits);
		}

		public BaseDataIterator()
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
			return new BaseData((Map)getHits().get(startOffset + hitCount++));
		}

		public void remove()
		{
		}
	
}
