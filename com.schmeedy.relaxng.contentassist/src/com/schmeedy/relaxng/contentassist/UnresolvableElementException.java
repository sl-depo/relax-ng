package com.schmeedy.relaxng.contentassist;

public class UnresolvableElementException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnresolvableElementException(String message) {
		super(message);
	}
	
	public UnresolvableElementException(String message, Exception e) {
		super(message, e);
	}
}
