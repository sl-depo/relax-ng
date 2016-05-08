package com.schmeedy.relaxng.contentassist.internal.function;

import javax.xml.namespace.QName;

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

public class ElementDerivativeFunction extends PatternFunctionAdapter {
	private QName contextElementName;
	
	public ElementDerivativeFunction(Element contextElement) {
		super();
		contextElementName = new QName(
				contextElement.getNamespaceURI(),
				contextElement.getLocalName());
	}

	@Override
	public Object caseElement(ElementPattern p) {
		if (!p.getNameClass().contains(contextElementName)) {
			return patternBuilder.makeNotAllowed();
		}
		return patternBuilder.makeEmpty();
	}

	@Override
	public Object caseGroup(GroupPattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		if (derivative1 instanceof NotAllowedPattern && p.getOperand1().isNullable()) {
			return p.getOperand2().apply(this);
		}
		return patternBuilder.makeGroup(derivative1, p.getOperand2());
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeChoice(derivative1, derivative2);
	}

	@Override
	public Object caseInterleave(InterleavePattern p) {
		Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		return patternBuilder.makeChoice(
					patternBuilder.makeInterleave(derivative1, p.getOperand2()),
					patternBuilder.makeInterleave(derivative2, p.getOperand1()));
	}
	
	@Override
	public Object caseOneOrMore(OneOrMorePattern p) {
		Pattern derivative = (Pattern) p.getOperand().apply(this);
		if (derivative instanceof NotAllowedPattern) {
			return patternBuilder.makeNotAllowed();
		}
		if (derivative instanceof EmptyPattern) {
			return patternBuilder.makeChoice(p, patternBuilder.makeEmpty());
		}
		return patternBuilder.makeGroup(derivative, p);
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
		return p;
	}
	
	@Override
	protected Object caseOther(Pattern p) {
		return p;
	}
}
