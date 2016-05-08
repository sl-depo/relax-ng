package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;

import com.schmeedy.relaxng.contentassist.internal.helper.CommonPattern;

public class AttributeEliminationFunction extends AbstractAttributeFunction {
	@Override
	public Object caseAttribute(AttributePattern p) {
		return patternBuilder.makeNotAllowed();
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeChoice(derivative1, derivative2);
	}
	
	// mostly because of attributes with open name classes, which can be found inside oneOrMore
	@Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		Pattern derivative = (Pattern) p.getOperand().apply(this);
		if (derivative instanceof NotAllowedPattern) {
			return CommonPattern.NOT_ALLOWED_PATTERN;
		} else {
			return p;
		}
	}
}
