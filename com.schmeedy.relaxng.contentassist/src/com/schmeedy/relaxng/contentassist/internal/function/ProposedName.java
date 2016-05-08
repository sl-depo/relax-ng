package com.schmeedy.relaxng.contentassist.internal.function;


public class ProposedName {
	private static final long serialVersionUID = 1L;
	
	private final String namespaceURI;
	
	private final String localPart;
	
	private final String prefix;
	
	private final String documentation;
	
	private boolean mandatory = true;
	
	public ProposedName(String namespaceURI, String localPart, String prefix, String documentation) {
		super();
		this.namespaceURI = "".equals(namespaceURI) ? null : namespaceURI;
		this.localPart = localPart;
		this.prefix = prefix;
		this.documentation = documentation;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public String getLocalPart() {
		return localPart;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getDocumentation() {
		return documentation;
	}

	void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localPart == null) ? 0 : localPart.hashCode());
		result = prime * result
				+ ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
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
		ProposedName other = (ProposedName) obj;
		if (localPart == null) {
			if (other.localPart != null)
				return false;
		} else if (!localPart.equals(other.localPart))
			return false;
		if (namespaceURI == null) {
			if (other.namespaceURI != null)
				return false;
		} else if (!namespaceURI.equals(other.namespaceURI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (mandatory ? "!" : "") + localPart + "{" + namespaceURI + "}";
	}
}
