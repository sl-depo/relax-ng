package com.schmeedy.relaxng.contentassist.internal;

import java.util.Map;

import com.schmeedy.relaxng.contentassist.IQNameCompletionProposal;
import com.schmeedy.relaxng.contentassist.internal.function.ProposedName;


public class DefaultQNameCompletionProposal implements IQNameCompletionProposal {
	private ProposedName proposedName;
	
	private Map<String, String> nsToPrefixMapping;
	
	private Map<String, String> inPlaceNsMapping;
	
	public DefaultQNameCompletionProposal(ProposedName proposedName, Map<String, String> nsToPrefixMapping, Map<String, String> inPlaceNsMapping) {
		this.proposedName = proposedName;
		this.nsToPrefixMapping = nsToPrefixMapping;
		this.inPlaceNsMapping = inPlaceNsMapping;
	}

	// @Override
	public String getLocalName() {
		return proposedName.getLocalPart();
	}

	// @Override
	public String getNamespaceURI() {
		return proposedName.getNamespaceURI();
	}

	// @Override
	public String getPrefix() {
		String namespaceURI = getNamespaceURI();
		if (namespaceURI == null || "".equals(namespaceURI)) {
			return null;
		}
		if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
			return "xml";
		}
		if (nsToPrefixMapping.containsKey(namespaceURI)) {
			return nsToPrefixMapping.get(namespaceURI);
		}
		if (inPlaceNsMapping.containsKey(namespaceURI)) {
			return inPlaceNsMapping.get(namespaceURI);
		}
		int prefixOrd = 1;		
		while (inPlaceNsMapping.containsValue("ns" + prefixOrd) || nsToPrefixMapping.containsValue("ns" + prefixOrd)) {
			prefixOrd++;
		}
		String prefix = "ns" + prefixOrd;
		inPlaceNsMapping.put(namespaceURI, prefix);
		return prefix;
	}

	public boolean isMandatory() {
		return proposedName.isMandatory();
	}
	
	// @Override
	public boolean isNSDeclarationRequired() {
		String namespaceURI = getNamespaceURI();
		if (namespaceURI == null || "".equals(namespaceURI) || "http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
			return false;
		}
		return !nsToPrefixMapping.containsKey(namespaceURI);
	}
	
	// @Override
	public String getDocumentation() {
		return proposedName.getDocumentation();
	}

	// @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((proposedName == null) ? 0 : proposedName.hashCode());
		return result;
	}

	// @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultQNameCompletionProposal other = (DefaultQNameCompletionProposal) obj;
		if (proposedName == null) {
			if (other.proposedName != null)
				return false;
		} else if (!proposedName.equals(other.proposedName))
			return false;
		return true;
	}
	
	// @Override
	public String toString() {
		return proposedName.toString();
	}
}
