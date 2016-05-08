package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.AfterPattern;
import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.DataExceptPattern;
import org.kohsuke.rngom.binary.DataPattern;
import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.ErrorPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.ListPattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.TextPattern;
import org.kohsuke.rngom.binary.ValuePattern;
import org.kohsuke.rngom.binary.visitor.PatternFunction;

public abstract class PatternFunctionAdapter implements PatternFunction {
	protected static PatternBuilder patternBuilder = PatternBuilder.getInstance();
	
	// @Override
	public Object caseAfter(AfterPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseAttribute(AttributePattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseChoice(ChoicePattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseData(DataPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseDataExcept(DataExceptPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseElement(ElementPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseEmpty(EmptyPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseError(ErrorPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseGroup(GroupPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseInterleave(InterleavePattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseList(ListPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseNotAllowed(NotAllowedPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseRef(RefPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseText(TextPattern p) {
		return caseOther(p);
	}

	// @Override
	public Object caseValue(ValuePattern p) {
		return caseOther(p);
	}
	
	protected abstract Object caseOther(Pattern p);
}
