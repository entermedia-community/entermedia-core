package org.openedit.node;

import java.util.List;

public interface NodeManager
{
	Node getLocalNode();

	String getLocalNodeId();

	String createDailySnapShot(String inCatalogId);

	String createSnapShot(String inCatalogId);

	List listSnapShots(String inCatalogId);

	void restoreSnapShot(String inCatalogId, String inSnapShotId);

	boolean connectCatalog(String inCatalogId);

	void deleteCatalog(String inId);
	
}