package com.openedit.util;

public class ExecResult {
	protected String fieldStandardOut;
	protected String fieldStandardError;
	protected boolean fieldRunOk;
	protected int fieldReturnValue;
	
	public String getStandardOut() {
		return fieldStandardOut;
	}
	public void setStandardOut(String standardOut) {
		fieldStandardOut = standardOut;
	}
	public String getStandardError() {
		return fieldStandardError;
	}
	public void setStandardError(String standardError) {
		fieldStandardError = standardError;
	}
	public boolean isRunOk() {
		return fieldRunOk;
	}
	public void setRunOk(boolean runOk) {
		fieldRunOk = runOk;
	}
	public int getReturnValue() {
		return fieldReturnValue;
	}
	public void setReturnValue(int returnValue) {
		fieldReturnValue = returnValue;
	}
	
}
