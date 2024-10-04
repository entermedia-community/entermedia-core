package org.openedit.hittracker;

import java.util.Collections;
import java.util.Iterator;

import org.openedit.Data;

public class EmptyHitTracker extends HitTracker {

	
	public EmptyHitTracker() {
		
	}
	
	@Override
	public Data get(int inCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return Collections.emptyIterator();
	}

	@Override
	public boolean contains(Object inHit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
