package org.openedit;

public interface Secured
{
	public boolean canRun(WebPageRequest inReq, String inMethodName);

}
