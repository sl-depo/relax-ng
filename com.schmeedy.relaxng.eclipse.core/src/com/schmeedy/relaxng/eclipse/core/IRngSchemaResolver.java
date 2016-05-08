package com.schmeedy.relaxng.eclipse.core;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.schmeedy.relaxng.contentassist.IRngSchema;

public interface IRngSchemaResolver {
	public IRngSchema getSchema(InputSource documentInputSource);
	
	public IRngSchema getSchema(Document document);
}