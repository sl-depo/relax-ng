package com.schmeedy.relaxng.eclipse.core.internal.contenttype;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.xml.sax.InputSource;

import com.schmeedy.relaxng.eclipse.core.IRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.core.internal.DefaultRngSchemaResolver;

public class RngDocumentContentDescriber extends XMLContentDescriber {
	private IRngSchemaResolver schemaResolver = DefaultRngSchemaResolver.INSTANCE;	
	
	private int checkRootNamespace(InputSource contents) throws IOException {
		if (schemaResolver.getSchema(contents) != null) {
			return VALID;
		}
		return INDETERMINATE;
	}

	public int describe(InputStream contents, IContentDescription description) throws IOException {
		if (super.describe(contents, description) == INVALID)
			return INVALID;
		contents.reset();
		return checkRootNamespace(new InputSource(contents));
	}

	public int describe(Reader contents, IContentDescription description) throws IOException {
		if (super.describe(contents, description) == INVALID)
			return INVALID;
		contents.reset();
		return checkRootNamespace(new InputSource(contents));
	}
}
