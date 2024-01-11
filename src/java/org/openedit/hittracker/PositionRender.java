package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


	public class PositionRender
	{
		public PositionRender(boolean isAcending)
		{
			setAsending(isAcending);
		}
		private static final Log log = LogFactory.getLog(PositionRender.class);
		protected boolean fieldAsending;//LowToHigh;
		protected int fieldPage = 1;
		protected int fieldHitsPerPage = -1;
		protected int fieldMaxPageListing = 6; //used for page listing
		protected long fieldSize;
		
		public long getSize()
		{
			return fieldSize;
		}

		public void setSize(long inSize)
		{
			fieldSize = inSize;
		}

		public int getMaxPageListing()
		{
			return fieldMaxPageListing;
		}

		public void setMaxPageListing(int inMaxPageListing)
		{
			fieldMaxPageListing = inMaxPageListing;
		}

		public boolean isAsending()
		{
			return fieldAsending;
		}

		public void setAsending(boolean inAsending)
		{
			fieldAsending = inAsending;
		}

		public List getCurrentPage()
		{
			return fieldCurrentPage;
		}

		public void setCurrentPage(List inCurrentPage)
		{
			fieldCurrentPage = inCurrentPage;
		}

		public void setTotalPages(int inTotalPages)
		{
			fieldTotalPages = inTotalPages;
		}
		protected List fieldCurrentPage;
		protected int fieldTotalPages;
		
		public int getHitsPerPage()
		{
			if( fieldHitsPerPage > -1)
			{
				return fieldHitsPerPage;
			}
			return 15;
		}

		public void setHitsPerPage(int inHitsPerPage)
		{
			if (inHitsPerPage > 0 && inHitsPerPage != fieldHitsPerPage)
			{
				fieldHitsPerPage = inHitsPerPage;
			}
		}
		/**
		 * One based
		 * @return
		 */
		public int getPage()
		{
			return fieldPage;
		}
		public void setPageOneBased(int inPageOneBased)
		{
			if( inPageOneBased == -1)
			{
				return;
			}
			if( fieldPage != inPageOneBased )
			{
				fieldCurrentPage = null;
				fieldPage = inPageOneBased;
			}
				
		}	
		public void setPageFromPosition(int inPosition)
		{
			if( isAsending())
			{
				fieldPage = getTotalPages()- inPosition + 1;
			}
			else
			{
				fieldPage = inPosition;  //1 =1 
			}
				
		}	
			
		public int getTotalPages()
		{
			return (int) fieldTotalPages;
		}

		
		//Reversed If we are on 3 previous would be 4
		public Integer prevPosition()
		{
			int page = getPosition() + 1;
			if (page > getTotalPages())
			{
				return null;
			}
			else
			{
				return page;
			}
		}
		//Reversed If we are on 3 next would be 2
		public Integer nextPosition()
		{
			int page = getPosition() - 1;
			if (page < 1)
			{
				return null;
			}
			else
			{
				return page;
			}
		}
		

		public int toPosition(int inPage)
		{
			//String sorted = getSearchQuery().getSortBy();
			if( isAsending() )
			{
				return inPage;
			}
			int position = getTotalPages() - inPage + 1;
			return position;
		}
		
		public int getStartingPosition()
		{
			int position = getTotalPages();
			if( isAsending())
			{
				position = 1;
			}
			return position;
		}
		public int getEndingPosition()
		{
			int position = 1;
			if( isAsending() )
				{
					position = getTotalPages();
				}
			return position;
		}
		public List pageRange()
		{
			int totalPages = getTotalPages();
			int page = getPage();
			int start = 1;

			if (page < getMaxPageListing() / 2) // under the first 5 records
			{
				start = 1;// - getMaxPageListing()/2;
			}
			else if (page + getMaxPageListing() / 2 + 1 >= totalPages) // near
			// the
			// end +
			// 1 for
			// the
			// selected
			// one
			{
				start = 1 + totalPages - getMaxPageListing(); // Make it start 10
				// from the end
				start = Math.max(1, start); // dont go below 1
			}
			else
			{
				start = 1 + page - getMaxPageListing() / 2;
			}

			int count = Math.min(totalPages, getMaxPageListing()); // what is
			// higher the
			// total count
			// or 10
			List hits = new ArrayList(count);
			for (int i = 0; i < count; i++)
			{
				hits.add(new Integer(start + i));
			}
			return hits;
		}

		public List pagesBefore(List pagerange)
		{
			int i = 0;
			int page = getPage();
			for (; i < pagerange.size(); i++)
			{
				Integer in = (Integer) pagerange.get(i);
				if (in.intValue() >= page)
				{
					break;
				}	
			}

			return pagerange.subList(0, i);
		}
		public List linksBefore()
		{
			List range = pageRange();
			return pagesBefore(range);
		}
		public List linksAfter()
		{
			List pagerange = pageRange();
			return pagesAfter(pagerange);
		}
		
		public List pagesAfter(List pagerange)
		{
			if (getTotalPages() == getPage())
			{
				return Collections.EMPTY_LIST;
			}
			if (pagerange.size() == 1) // Only one hit
			{
				return Collections.EMPTY_LIST;
			}
			List before = pagesBefore(pagerange);
			List after = pagerange.subList(before.size() + 1, pagerange.size());
			return after;
//			int i = 0;
//			int position = getPosition();
//			for (; i < range.size(); i++)
//			{
//				Integer in = (Integer) range.get(i);
//				if( isAsending())
//				{
//					if (in.intValue() >= position)
//					{
//						break;
//					}
//				}
//				else
//				{
//					if (in.intValue() < position)
//					{
//						break;
//					}					
//				}
//				
//			}
			
			//return range.subList(i, range.size());

		}

		public int getPosition()
		{
			if( isAsending())
			{
				return getPage();
			}
			else
			{
				return getTotalPages() - getPage() + 1;
			}
		}
	
		public Links links()
		{
			Links links = new Links();
			links.setCurrentPosition(getPosition());
			List pagerange = pageRange();
			if( isAsending())  //Normal pagination
			{
				links.setBefore(pagesBefore(pagerange));
				links.setAfter(pagesAfter(pagerange));
			}
			else //Flip these and sort reverse
			{			
				List before = pagesAfter(pagerange);
				Collections.reverse(before);
				links.setBefore(before);
				
				List after = pagesBefore(pagerange);
				Collections.reverse(after);
				links.setAfter(after);
			}
			return links;
		}
}