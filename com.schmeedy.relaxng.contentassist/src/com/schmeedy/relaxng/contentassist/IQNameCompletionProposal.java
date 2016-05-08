package com.schmeedy.relaxng.contentassist;

public interface IQNameCompletionProposal {
	boolean isNSDeclarationRequired();
	
	boolean isMandatory();
	
	String getDocumentation();
	
	String getPrefix();
	
	String getLocalName();
	
	String getNamespaceURI();
}
