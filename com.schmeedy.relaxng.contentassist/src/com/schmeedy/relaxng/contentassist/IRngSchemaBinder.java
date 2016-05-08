package com.schmeedy.relaxng.contentassist;

import java.io.IOException;

import org.w3c.dom.Document;

public interface IRngSchemaBinder {
	void bind(Document document, IRngSchema schema) throws IOException, InvalidRelaxNgSchemaException;
	
	void unBind(Document document);
	
	boolean hasBoundSchema(Document document);
}
