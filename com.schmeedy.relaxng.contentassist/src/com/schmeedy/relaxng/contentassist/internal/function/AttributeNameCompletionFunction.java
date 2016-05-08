package com.schmeedy.relaxng.contentassist.internal.function;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.nc.SimpleNameClass;



public class AttributeNameCompletionFunction extends AbstractQNameCompletionFunction {

	@Override
	public Object caseAttribute(AttributePattern p) {
		if (p.getNameClass() instanceof SimpleNameClass) {
			QName name = ((SimpleNameClass) p.getNameClass()).name;
			String doc = p.getDocumentation() != null ? p.getDocumentation().getText() : null;
			return new ProposedName[] {new ProposedName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix(), doc)};
		}
		return null;
	}
	
	@Override
	public Object caseGroup(GroupPattern p) {
		return caseBranchingPattern(p, false);
	}

}
