package com.schmeedy.relaxng.contentassist.internal.function;

import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;

public class PatternBuilder {
	private static PatternBuilder instance;

	public synchronized static PatternBuilder getInstance() {
		if (instance == null) {
			instance = new PatternBuilder();
		}
		return instance;
	}

	private NotAllowedPattern notAllowed = new NotAllowedPattern();
	private EmptyPattern empty = new EmptyPattern();

	private PatternBuilder() {}

	public Pattern makeChoice(Pattern p1, Pattern p2) {
		if (p1 instanceof NotAllowedPattern) {
			if (p2 instanceof NotAllowedPattern) {
				return p1;
			} else {
				return p2;
			}
		} else if (p2 instanceof NotAllowedPattern) {
			return p1;
		} else {
			if (p1 instanceof EmptyPattern && p2.isNullable())
				return p2;
			if (p2 instanceof EmptyPattern && p1.isNullable())
				return p1;
			return new ChoicePattern(p1, p2);
		}
	}

	public Pattern makeGroup(Pattern p1, Pattern p2) {
		if (p1 instanceof EmptyPattern)
			return p2;
		if (p2 instanceof EmptyPattern)
			return p1;
		if (p1 instanceof NotAllowedPattern || p2 instanceof NotAllowedPattern)
			return notAllowed;
		return new GroupPattern(p1, p2);
	}

	public Pattern makeInterleave(Pattern p1, Pattern p2) {
		if (p1 instanceof EmptyPattern)
			return p2;
		if (p2 instanceof EmptyPattern)
			return p1;
		if (p1 instanceof NotAllowedPattern || p2 instanceof NotAllowedPattern)
			return notAllowed;
		return new InterleavePattern(p1, p2);	    
	}

	public Pattern makeOneOrMore(Pattern p) {
		if (p instanceof EmptyPattern || p instanceof NotAllowedPattern || p instanceof OneOrMorePattern)
			return p;
		return new OneOrMorePattern(p);
	}

	public Pattern makeOptional(Pattern p) {
		return makeChoice(p, empty);
	}

	public Pattern makeZeroOrMore(Pattern p) {
		return makeOptional(makeOneOrMore(p));
	}
	
	public Pattern makeEmpty() {
		return empty;
	}
	
	public Pattern makeNotAllowed() {
		return notAllowed;
	}
}
