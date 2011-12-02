package org.entermedia.locks;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditException;

public class MemoryLockManager implements LockManager
{
	private static final Log log = LogFactory.getLog(MemoryLockManager.class);

	protected Map<String,Lock> fieldLocks;
	protected long fieldLockId = 0;
	
	protected Map<String,Lock> getLocks()
	{
		if (fieldLocks == null)
		{
			fieldLocks = new ConcurrentHashMap<String,Lock>();
		}
		return fieldLocks;
	}

	public Lock lock(String inCatId, String inPath, String inOwnerId)
	{
		Lock lock = loadLock(inCatId, inPath);
		int tries = 0;
		while( !grabLock(inCatId, inOwnerId, lock))
		{
			tries++;
			if( tries > 9)
			{
				throw new OpenEditException("Could not lock file " + inPath + " locked by " + lock.getNodeId() + " " + lock.getOwnerId() );
			}
			try
			{
				Thread.sleep(250);
			}
			catch( Exception ex)
			{
				//does not happen
				log.info(ex);
			}
			log.info("Could not lock " + inPath + " trying again  " + tries);
			lock = loadLock(inCatId, inPath);
		}
		return lock;

	}

	public boolean isOwner(String inCatId, Lock lock)
	{
		if( lock == null)
		{
			throw new OpenEditException("Lock should not be null");
		}
		if( lock.getId() == null)
		{
			throw new OpenEditException("lock id is currently null");
		}

		Lock owner = loadLock(inCatId, lock.getPath());
		if( owner == null)
		{
			throw new OpenEditException("Owner lock is currently null");
		}
		if( lock.getOwnerId() == null)
		{
			return false;
		}
		boolean sameowner = lock.getOwnerId().equals(owner.getOwnerId());
		return sameowner;
	}

	protected long nextId()
	{
		return fieldLockId++;
	}
	@Override
	public Lock loadLock(String inCatId, String inPath)
	{
		Lock lock = getLocks().get(inPath);
		if( lock == null)
		{
			synchronized (getLocks())
			{
				lock = getLocks().get(inPath);
				if( lock == null)
				{
					lock = addLock(inPath, null);
					getLocks().put(inPath,lock);
				}
			}
		}
		return lock;
	}

	@Override
	public Collection getLocksByDate(String inCatId, String inPath)
	{
		throw new OpenEditException("Not implemented");
	}

	@Override
	public boolean release(String inCatId, Lock inLock)
	{
		inLock.setLocked(false);
		return true;
		
	}

	@Override
	public void releaseAll(String inCatalogId, String inPath)
	{
		getLocks().clear();
	}

	
	
	protected Lock addLock(String inPath, String inOwnerId)
	{
		Lock lockrequest = new Lock();
		lockrequest.setId(String.valueOf( nextId() ) );
		lockrequest.setPath(inPath);
		lockrequest.setOwnerId(inOwnerId);
		lockrequest.setDate(new Date());
		lockrequest.setNodeId("inmemory");
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		return lockrequest;
	}

	public boolean grabLock(String inCatId, String inOwner, Lock lock)
	{
		if( lock == null)
		{
			throw new OpenEditException("Lock should not be null");
		}

		if( lock.isLocked())
		{
			return false;
		}
		//set owner
		try
		{
			lock.setOwnerId(inOwner);
			lock.setDate(new Date());
			lock.setLocked(true);
			saveLock(lock);
		}
		catch( ConcurrentModificationException ex)
		{
			return false;
		}
		return true;
			
	}

	public Lock lockIfPossible(String inCatId, String inPath, String inOwnerId)
	{
		Lock lock = loadLock(inCatId, inPath);
		
		if( grabLock(inCatId, inOwnerId, lock) )
		{
			return lock;
		}
		return null;
	}
	
	private void saveLock(Lock inLock)
	{
		//TODO: Throw error if someone else saved this lock
	}

}
