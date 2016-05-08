package com.schmeedy.relaxng.contentassist.internal.function;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kohsuke.rngom.binary.BinaryPattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.RefPattern;


public abstract class AbstractQNameCompletionFunction extends PatternFunctionAdapter {
	// group is handled differently in case of attributes / elements
	@Override
	public abstract Object caseGroup(GroupPattern p);

	private Object getCompletionsFromBranch(Object o) {
		if (o instanceof ProposedName[]) {
			return o;
		}
		if (o instanceof Pattern) {
			return ((Pattern)o).apply(this);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected Object caseBranchingPattern(BinaryPattern bp, boolean bothBranchesToBeMandatory) {
		ProposedName[] opt1 = (ProposedName[])getCompletionsFromBranch(bp.getOperand1());
		ProposedName[] opt2 = (ProposedName[])getCompletionsFromBranch(bp.getOperand2());
		if (opt1 == null) {
			if (bothBranchesToBeMandatory && opt2 != null) {
				makeOptional(opt2);
			}
			return opt2;
		}
		if (opt2 == null) {
			if (bothBranchesToBeMandatory && opt1 != null) {
				makeOptional(opt1);
			}
			return opt1;
		}
		
		HashSet<ProposedName> firstBranch = new HashSet<ProposedName>(Arrays.asList(opt1));
		Set<ProposedName> secondBranch = new HashSet<ProposedName>(Arrays.asList(opt2));
		Set<ProposedName> combined = (Set<ProposedName>) firstBranch.clone();
		combined.addAll(secondBranch);
		
		if (bothBranchesToBeMandatory) {
			removeOptional(firstBranch);
			removeOptional(secondBranch);
			for (ProposedName proposedName : combined) {
				if (firstBranch.contains(proposedName) && secondBranch.contains(proposedName)) {
					proposedName.setMandatory(true);
				} else {
					proposedName.setMandatory(false);
				}
			}
		}
		return combined.toArray(new ProposedName[combined.size()]);
	}
	
	private void removeOptional(Set<ProposedName> names) {
		Iterator<ProposedName> i = names.iterator();
		while (i.hasNext()) {
			if (!i.next().isMandatory()) {
				i.remove();
			}
		}
	}
	
	private void makeOptional(ProposedName[] proposals) {
		for (ProposedName proposal : proposals) {
			proposal.setMandatory(false);
		}
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		return caseBranchingPattern(p, true);
	}
	
	@Override
	public Object caseInterleave(InterleavePattern p) {
		return caseBranchingPattern(p, false);
	}
	
	@Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		return p.getOperand().apply(this);
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
