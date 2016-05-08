package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.Pattern;

public abstract class AbstractAttributeFunction extends PatternFunctionAdapter {	
	@Override
	public Object caseGroup(GroupPattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeGroup(derivative1, derivative2);
	}
	
	@Override
	public Object caseInterleave(InterleavePattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeInterleave(derivative1, derivative2);
	}
	
	@Override
	protected Object caseOther(Pattern p) {
		return p;
	}
}
