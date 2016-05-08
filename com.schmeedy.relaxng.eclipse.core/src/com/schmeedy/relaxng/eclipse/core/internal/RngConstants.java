package com.schmeedy.relaxng.eclipse.core.internal;

public final class RngConstants {
	public static final String BINDING_INSTRUCTION_TARGET = "oasis-schema";
	public static final String BINDING_INSTRUCTION_URI_PSEUDO_ATT_NAME = "href";
	public static final String BINDING_INSTRUCTION_TYPE_PSEUDO_ATT_NAME = "type";
	public static final String BINDING_INSTRUCTION_MODE_PSEUDO_ATT_NAME = "mode";
	public static final String BINDING_INSTRUCTION_MODE_PSEUDO_ATT_VALUE = "edit";
	
	public static final String MIME_XML1 = "application/xml";
	public static final String MIME_XML2 = "text/xml";
	public static final String MIME_RNC = "application/relax-ng-compact-syntax";
	
	public static final String CONTENT_TYPE_XML = "org.eclipse.core.runtime.xml";
	public static final String CONTENT_TYPE_RNG_SCHEMA = "com.schmeedy.relaxng.eclipse.core.relaxngSchema";
	public static final String CONTENT_TYPE_RNC_SCHEMA = "com.schmeedy.relaxng.eclipse.core.relaxngSchemaCompact";
	public static final String CONTENT_TYPE_RELAX_DOCUMENT = "com.schmeedy.relaxng.eclipse.core.relaxngDocument";
	
	private RngConstants() {}
}
