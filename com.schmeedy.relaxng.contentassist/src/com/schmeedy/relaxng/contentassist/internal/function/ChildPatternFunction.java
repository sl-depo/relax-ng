package com.schmeedy.relaxng.contentassist.internal.function;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.TextPattern;
import org.w3c.dom.Element;

public class ChildPatternFunction extends PatternFunctionAdapter {
	private QName contextElementName;
	
	public ChildPatternFunction(Element contextElement) {
		super();
		contextElementName = new QName(
				contextElement.getNamespaceURI(),
				contextElement.getLocalName());
	}

	@Override
	public Object caseElement(ElementPattern p) {
		if (!p.getNameClass().contains(contextElementName)) {
			return patternBuilder.makeNotAllowed();
		} else {
			return p.getContent();
		}
	}
	
	@Override
	public Object caseAttribute(AttributePattern p) {
		return patternBuilder.makeNotAllowed();
	}

	@Override
	public Object caseGroup(GroupPattern p) {
		if (p.getOperand1().isNullable()) {
			return patternBuilder.makeChoice((Pattern) p.getOperand1().apply(this), (Pattern) p.getOperand2().apply(this));
		}
		return p.getOperand1().apply(this);
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		Pattern child1 = (Pattern) p.getOperand1().apply(this);
		Pattern child2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeChoice(child1, child2);
	}

	@Override
	public Object caseInterleave(InterleavePattern p) {
		Pattern child1 = (Pattern) p.getOperand1().apply(this);
		Pattern child2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeChoice(child1, child2);
	}
	
	@Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		return p.getOperand().apply(this);
	}
	
	@Override
	public Object caseEmpty(EmptyPattern p) {
		return patternBuilder.makeNotAllowed();
	}

	@Override
	public Object caseNotAllowed(NotAllowedPattern p) {
		return p;
	}

	@Override
	public Object caseRef(RefPattern p) {
		return p.getPattern().apply(this);
	}
	
	@Override
	public Object caseText(TextPattern p) {
		return patternBuilder.makeNotAllowed();
	}

	@Override
	protected Object caseOther(Pattern p) {		
		return patternBuilder.makeNotAllowed();
	}
}
