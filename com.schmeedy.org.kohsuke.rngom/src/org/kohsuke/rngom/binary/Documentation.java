package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

public class Documentation implements ParsedElementAnnotation {
	private String text;

	public Documentation(String text) {
		super();
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
