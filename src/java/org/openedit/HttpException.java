package org.openedit;

public class HttpException extends OpenEditException
{

	int errorcode;
	
	
	public HttpException(String inMsg, String inPath, int inErrorCode)
	{
		super(inMsg, null, inPath);
		setErrorcode(inErrorCode);
	}

	public int getErrorcode()
	{
		return errorcode;
	}

	public void setErrorcode(int inErrorcode)
	{
		errorcode = inErrorcode;
	}
	
}
