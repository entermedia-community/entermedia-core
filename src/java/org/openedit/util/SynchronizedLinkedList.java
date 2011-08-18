package org.openedit.util;

import java.util.LinkedList;

public class SynchronizedLinkedList<E> extends LinkedList<E>
{

	public synchronized boolean remove(Object o) 
	{
		return super.remove(o);
	}

	@Override
	public synchronized void push(E inE)
	{
		super.push(inE);
	}

	@Override
	public synchronized Object[] toArray()
	{
		return super.toArray();
	}
	
	
}
