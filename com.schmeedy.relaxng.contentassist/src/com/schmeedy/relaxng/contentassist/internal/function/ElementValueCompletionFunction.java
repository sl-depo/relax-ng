package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.Pattern;

public class ElementValueCompletionFunction extends AbstractValueCompletionFunction {
	public ElementValueCompletionFunction(String[] ids) {
		super(ids);
	}
	
	@Override
	public Object caseGroup(GroupPattern p) {
		Pattern operand1 = p.getOperand1();
		if (operand1.isNullable()) {
			return caseBranchingPattern(p);
		} else {
			return p.getOperand1().apply(this);
		}
	}
}
