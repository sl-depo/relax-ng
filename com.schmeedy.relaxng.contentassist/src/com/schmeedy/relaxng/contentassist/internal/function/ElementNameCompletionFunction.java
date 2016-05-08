package com.schmeedy.relaxng.contentassist.internal.function;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.nc.SimpleNameClass;



public class ElementNameCompletionFunction extends AbstractQNameCompletionFunction {

	@Override
	public Object caseElement(ElementPattern p) {
		if (p.getNameClass() instanceof SimpleNameClass) {
			QName name = ((SimpleNameClass) p.getNameClass()).name;
			String doc = p.getDocumentation() != null ? p.getDocumentation().getText() : null;
			return new ProposedName[] {new ProposedName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix(), doc)};
		}
		return null;
	}
		
	@Override
	public Object caseGroup(GroupPattern p) {
		Pattern operand1 = p.getOperand1();
		if (operand1.isNullable()) {
			return caseBranchingPattern(p, false);
		} else {
			return p.getOperand1().apply(this);
		}
	}
}
