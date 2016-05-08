package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class JingValidationException extends SAXParseException {
	private static final long serialVersionUID = 1L;

	private final String problemId;
	
	private final String messageArg;
	
	public JingValidationException(String problemId, Locator locator) {
		super(SchemaBuilderImpl.localizer.message(problemId), locator);
		this.problemId = problemId;
		this.messageArg = null;
	}
	
	public JingValidationException(String problemId, String messageArg, Locator locator) {
		super(SchemaBuilderImpl.localizer.message(problemId, messageArg), locator);
		this.problemId = problemId;
		this.messageArg = messageArg;
	}
	
	public String getProblemId() {
		return problemId;
	}
	
	public String getMessageArg() {
		return messageArg;
	}
}
