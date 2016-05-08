package com.schmeedy.relaxng.contentassist.internal.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler {
	private static final Log logger = LogFactory.getLog(DefaultErrorHandler.class);
	
	// @Override
	public void error(SAXParseException exception) throws SAXException {
		throw exception; 
	}

	// @Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw exception;
	}

	// @Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn("SAX Warning: " + exception.getMessage());
	}

}
