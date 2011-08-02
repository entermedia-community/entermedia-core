package org.entermedia.locks;

import java.util.Collection;

public interface LockManager
{

	public abstract Lock lock(String inCatId, String inPath, String inOwnerId);

	public abstract Lock loadLock(String inCatId, String inPath);

	public abstract Collection getLocksByDate(String inCatId, String inPath);

	public abstract Lock lockIfPossible(String inCatId, String inPath, String inOwnerId);

	public abstract boolean release(String inCatId, Lock inLock);

	public abstract void releaseAll(String inCatalogId, String inPath);

	public boolean isOwner(Lock lock, String inOwnerId);

}