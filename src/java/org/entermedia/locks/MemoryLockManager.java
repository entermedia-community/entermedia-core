package org.entermedia.locks;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.data.Searcher;
import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditException;

public class MemoryLockManager implements LockManager
{
	private static final Log log = LogFactory.getLog(MemoryLockManager.class);

	protected Map<String,Stack> fieldLocks;
	protected long fieldLockId = 0;
	
	protected Map<String,Stack> getLocks()
	{
		if (fieldLocks == null)
		{
			fieldLocks = new ConcurrentHashMap<String,Stack>();
		}
		return fieldLocks;
	}

	public Lock lock(String inCatId, String inPath, String inOwnerId)
	{
		Lock lock = addLock(inPath, inOwnerId);

		int tries = 0;
		while( !isOwner(inCatId,lock))
		{
			tries++;
			log.info("Could not lock trying again  " + tries);
			if( tries > 9)
			{
				release(inCatId,lock);
				throw new OpenEditException("Could not lock file " + inPath + " locked by " + lock.getOwnerId() );
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
		}
		return lock;

	}

	protected long nextId()
	{
		return fieldLockId++;
	}
	@Override
	public Lock loadLock(String inCatId, String inPath)
	{
		
		Stack stack = getStack(inPath);
		if( stack.empty())
		{
			return null;
		}
		Lock lock = (Lock)stack.firstElement();
		return lock;
	}

	@Override
	public Collection getLocksByDate(String inCatId, String inPath)
	{
		Stack stack = getStack(inPath);
		return stack;
	}

	@Override
	public Lock lockIfPossible(String inCatId, String inPath, String inOwnerId)
	{
		Lock lock = addLock(inPath, inOwnerId);

		if( isOwner(inCatId	,lock))
		{
			return lock;
		}
		release(inCatId, lock);
		return null;
	}

	@Override
	public boolean release(String inCatId, Lock inLock)
	{
		Stack<Lock> locks = getStack(inLock.getPath());
		return locks.remove(inLock);
	}

	@Override
	public void releaseAll(String inCatalogId, String inPath)
	{
		getLocks().clear();
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
	
	protected Lock addLock(String inPath, String inOwnerId)
	{
		Lock lockrequest = new Lock();
		lockrequest.setPath(inPath);
		lockrequest.setOwnerId(inOwnerId);
		lockrequest.setDate(new Date());
		lockrequest.setNodeId("inmemory");
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		push(lockrequest);
		return lockrequest;
	}

	protected void push(Lock inLockrequest)
	{
		Stack locks = getStack(inLockrequest.getPath());
		locks.push(inLockrequest);
	}

	protected Stack getStack(String inPath)
	{
		Stack locks = getLocks().get(inPath);
		if( locks == null)
		{
			locks = new Stack<Lock>();
			getLocks().put(inPath,locks);
		}
		return locks;
	}

}
