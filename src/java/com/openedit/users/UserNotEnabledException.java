package com.openedit.users;

public class UserNotEnabledException extends UserManagerException {

	public UserNotEnabledException() {
	}

	public UserNotEnabledException(String message) {
		super(message);
	}

	public UserNotEnabledException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserNotEnabledException(Throwable cause) {
		super(cause);
	}

}
