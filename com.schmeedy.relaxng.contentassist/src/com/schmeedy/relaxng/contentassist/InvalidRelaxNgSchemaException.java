package com.schmeedy.relaxng.contentassist;

public class InvalidRelaxNgSchemaException extends Exception {
	public InvalidRelaxNgSchemaException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRelaxNgSchemaException(String message) {
		super(message);
	}

	public InvalidRelaxNgSchemaException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
