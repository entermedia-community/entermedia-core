package org.openedit.users;

import org.openedit.data.Searcher;

public interface GroupSearcher extends Searcher
{

	public abstract Group getGroup(String inGroupId);

}