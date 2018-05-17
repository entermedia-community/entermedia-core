package org.openedit.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NodeManager
{
	Node getLocalNode();

	String getLocalNodeId();
	
	String createDailySnapShot(String inCatalogId);

	String createSnapShot(String inCatalogId);

	List listSnapShots(String inCatalogId);

	void restoreSnapShot(String inCatalogId, String inSnapShotId);

	public boolean containsCatalog(String inCatalogId);
	
	boolean connectCatalog(String inCatalogId);

	void deleteCatalog(String inId);
	
	public boolean reindexInternal(String inCatalogId);

	void clear();
	
	public String getClusterHealth();
	
	public Collection getRemoteNodeList(String inCatalog);
	
}