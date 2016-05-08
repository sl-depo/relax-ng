package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.BinaryPattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.DataPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.ValuePattern;
import org.relaxng.datatype.Datatype;

public abstract class AbstractValueCompletionFunction extends PatternFunctionAdapter {
	// group is handled differently in case of attributes / elements
	@Override
	public abstract Object caseGroup(GroupPattern p);

	private String[] ids;
	
	AbstractValueCompletionFunction(String[] ids) {
		this.ids = ids;
	}
	
	private Object getCompletionsFromBranch(Object o) {
		if (o instanceof String[]) {
			return o;
		}
		if (o instanceof Pattern) {
			return ((Pattern)o).apply(this);
		}
		return null;
	}
	
	protected Object caseBranchingPattern(BinaryPattern bp) {
		String[] opt1 = (String[])getCompletionsFromBranch(bp.getOperand1());
		String[] opt2 = (String[])getCompletionsFromBranch(bp.getOperand2());
		if (opt1 == null) {
			return opt2;
		}
		if (opt2 == null) {
			return opt1;
		}
		String[] result = new String[opt1.length + opt2.length];
		for (int i = 0; i < opt1.length; i++) {
			result[i] = opt1[i];
		}
		for (int j = 0; j < opt2.length; j++) {
			result[opt1.length + j] = opt2[j];
		}
		return result;
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		return caseBranchingPattern(p);
	}
	
	@Override
	public Object caseInterleave(InterleavePattern p) {
		return caseBranchingPattern(p);
	}
	
	@Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		return p.getOperand().apply(this);
	}
	
	@Override
	public Object caseValue(ValuePattern p) {
		return new String[] { String.valueOf(p.getValue()) };
	}
	
	@Override
	public Object caseData(DataPattern p) {
		if (p.getDatatype().getIdType() == Datatype.ID_TYPE_IDREF) {
			return ids;
		}
		return null;
	}
	
	@Override
	public Object caseRef(RefPattern p) {
		return p.apply(this);
	}
	
	@Override
	protected Object caseOther(Pattern p) {
		return null;
	}
}