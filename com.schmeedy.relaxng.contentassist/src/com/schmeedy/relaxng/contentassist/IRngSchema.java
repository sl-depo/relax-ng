package com.schmeedy.relaxng.contentassist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface IRngSchema {	
	InputStream openInputStream() throws IOException;
	
	RngSchemaSyntax getSchemaSyntax();
	
	URI getSchemaUri();
	
	public enum RngSchemaSyntax {
		COMPACT,
		XML;
	}
}
