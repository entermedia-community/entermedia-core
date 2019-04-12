package org.openedit.locks;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.util.DateStorageUtil;

public class MemoryLockManager implements LockManager
{
	private static final Log log = LogFactory.getLog(MemoryLockManager.class);

	protected Map<String,Lock> fieldLocks;
	protected long fieldLockId = 0;
	protected String fieldCatalogId;
	
	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	protected Map<String,Lock> getLocks()
	{
		if (fieldLocks == null)
		{
			fieldLocks = new ConcurrentHashMap<String,Lock>();
		}
		return fieldLocks;
	}

	public Lock lock(String inPath, String inOwnerId)
	{
		Lock lock = loadLock(inPath);
		int tries = 0;
		while( !grabLock( inOwnerId, lock))
		{
			tries++;
			if( tries > 20)
			{
				throw new OpenEditException("Could not lock file " + inPath + " locked by " + lock.getNodeId() + " " + lock.getOwnerId() );
			}
			try
			{
				Thread.sleep(500);
			}
			catch( Exception ex)
			{
				//does not happen
				log.info(ex);
			}
			log.info("Could not lock " + inPath + " trying again  " + tries  + " locked by " + lock.getNodeId() + " " + lock.getOwnerId() );
			lock = loadLock(inPath);
		}
		return lock;

	}

	public boolean isOwner(Lock lock)
	{
		if( lock == null)
		{
			throw new OpenEditException("Lock should not be null");
		}
		if( lock.getId() == null)
		{
			throw new OpenEditException("lock id is currently null");
		}

		Lock owner = loadLock(lock.getSourcePath());
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
	public Lock loadLock(String inPath)
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
	public Collection getLocksByDate(String inPath)
	{
		throw new OpenEditException("Not implemented");
	}

	@Override
	public boolean release(Lock inLock)
	{
		if( inLock == null)
		{
			return true;
		}
		inLock.setLocked(false);
		return true;
		
	}

	@Override
	public void releaseAll(String inPath)
	{
		getLocks().clear();
	}

	
	
	protected Lock addLock(String inPath, String inOwnerId)
	{
		Lock lockrequest = new Lock();
		lockrequest.setId(String.valueOf( nextId() ) );
		lockrequest.setSourcePath(inPath);
		lockrequest.setOwnerId(inOwnerId);
		lockrequest.setDate(new Date());
		lockrequest.setNodeId("inmemory");
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		return lockrequest;
	}

	public boolean grabLock(String inOwner, Lock lock)
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
	/**
	 * Tries once then gives up and returns null
	 */
	public Lock lockIfPossible(String inPath, String inOwnerId)
	{
		Lock lock = loadLock(inPath);
		
		if( grabLock(inOwnerId, lock) )
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
