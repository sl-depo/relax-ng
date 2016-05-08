package com.schmeedy.relaxng.eclipse.core.internal.binding;

import java.net.URI;

import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;

public class RngSchemaBinding {
	private String namespace;
	
	private URI schemaUri;
	
	private RngSchemaSyntax rngSchemaSyntax;
	
	public RngSchemaBinding(String namespace, URI schemaUri, RngSchemaSyntax rngSchemaSyntax) {
		super();
		this.namespace = namespace;
		this.schemaUri = schemaUri;
		this.rngSchemaSyntax = rngSchemaSyntax;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public URI getSchemaUri() {
		return schemaUri;
	}
	
	public RngSchemaSyntax getSchemaSyntax() {
		return rngSchemaSyntax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result
				+ ((schemaUri == null) ? 0 : schemaUri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RngSchemaBinding other = (RngSchemaBinding) obj;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (schemaUri == null) {
			if (other.schemaUri != null)
				return false;
		} else if (!schemaUri.equals(other.schemaUri))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return namespace + "=" + schemaUri;
	}
}