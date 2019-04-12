package org.openedit.locks;

import java.util.Collection;

import org.openedit.CatalogEnabled;

public interface LockManager extends CatalogEnabled
{

	public abstract Lock lock(String inPath, String inOwnerId);

	public abstract Lock loadLock(String inPath);

	public abstract Collection<Lock> getLocksByDate(String inPath);

	public abstract Lock lockIfPossible(String inPath, String inOwnerId);

	public abstract boolean release(Lock inLock);

	public abstract void releaseAll(String inPath);

	public boolean isOwner(Lock lock);
	
	String getCatalogId();

	void setCatalogId(String inId);
}