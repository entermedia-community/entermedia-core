package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.List;

import com.openedit.BaseTestCase;
import com.openedit.hittracker.ListHitTracker;

public class HitTest extends BaseTestCase
{
	
	public void testRows() throws Exception
	{
		ListHitTracker tracker = new ListHitTracker();
		tracker.setHitsPerPage(10);

		Integer first = null;
		List range  = null;
		
		ArrayList some = new ArrayList();
		for (int i = 0; i < 25; i++)
		{
			some.add(new Integer(i));
		}
		tracker.setList(some);
		tracker.setPage(2);
		range = tracker.linkRange();
		assertEquals(3,range.size());
		first = (Integer)range.get(0);
		assertEquals(new Integer(1), first);

		
		ArrayList all = new ArrayList();
		for (int i = 0; i < 150; i++)
		{
			all.add(new Integer(i));
		}
		tracker.setList(all);


		tracker.setPage(10);
		range = tracker.linkRange();
		assertEquals(10,range.size());
		first = (Integer)range.get(0);
		assertEquals(new Integer(6), first);


		tracker.setPage(6);
		List range6 = tracker.linkRange();
		assertEquals(10,range6.size());
		Integer first2 = (Integer)range6.get(0);
		assertEquals(new Integer(2), first2);


		//1 2 3 4 5 6 7 8 9 10 11 *12* 13 14 15		
		tracker.setPage(12);
		range = tracker.linkRange();
		assertEquals(10,range.size());
		Integer last = (Integer)range.get(range.size() -1 );
		assertEquals(new Integer(15), last);
		first = (Integer)range.get(0);
		assertEquals(new Integer(6), first);
		tracker.setPage(13);
		range = tracker.linkRange();
		assertEquals(10,range.size());
		first = (Integer)range.get(0);
		assertEquals(new Integer(6), first);
		tracker.setPage(15);
		range = tracker.linkRange();
		assertEquals(10,range.size());
		first = (Integer)range.get(0);
		assertEquals(new Integer(6), first);

		
		tracker.setPage(2);
		range= tracker.linkRange();
		assertEquals(10,range.size());
		first = (Integer)range.get(0);
		assertEquals(new Integer(1), first);

		
		tracker.setPage(6);
		
		range = tracker.linksBefore();
		assertEquals(4,range.size());
		Integer bselect = (Integer)range.get(3);
		assertEquals(new Integer(5), bselect);
		
		range = tracker.linksAfter();
		Integer select = (Integer)range.get(0);
		assertEquals(new Integer(7), select);
		assertEquals(5,range.size());

		
		
		
	}

}
