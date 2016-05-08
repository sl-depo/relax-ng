package com.schmeedy.relaxng.contentassist.internal.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.ValuePattern;
import org.relaxng.datatype.Datatype;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class AttributeDerivativeFunction extends AbstractAttributeFunction {
	private static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
	private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
	private static final String RESERVED_XML_PREFIX = "xml";
		
	private Map<QName, String> attMap = new HashMap<QName, String>();
	
	private Stack<ChoiceBranch> branchStack = new Stack<ChoiceBranch>();
	
	public AttributeDerivativeFunction(Element contextElement, Attr except) {
		super();
		
		NamedNodeMap attributes = contextElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			if (XMLNS_NAMESPACE.equals(attribute.getNamespaceURI()) || attribute.equals(except)) {
				continue;
			}
			QName attName = createAttQName(attribute);
			attMap.put(attName, attribute.getValue());
		}
	}
	
	private final QName createAttQName(Attr attribute) {
		String namespace;
		if (RESERVED_XML_PREFIX.equals(attribute.getPrefix()) && attribute.getNamespaceURI() == null) {
			namespace = XML_NAMESPACE;
		} else {
			namespace = attribute.getNamespaceURI();
		}
		return new QName(namespace,	attribute.getLocalName());
	}
	
	@Override
	public Object caseAttribute(AttributePattern p) {
		for (Entry<QName, String> attEntry: attMap.entrySet()) {		
			if (p.getNameClass().contains(attEntry.getKey())) {
				Pattern content = p.getContent();
				if (content instanceof ValuePattern) {
					if (((ValuePattern)content).getValue().equals(attEntry.getValue())) {
						return matched();
					} else {
						return unmatched();
					}
				}
				if (content instanceof Datatype) {
					Datatype dt = (Datatype) content;
					if (!dt.isContextDependent()) {
						if (dt.isValid(attEntry.getValue(), null)) {
							return matched();
						} else {
							return unmatched();
						}
					}
				}
				return matched();
			}
		}
		return p;
	}
	
	private Pattern matched() {
		if (!branchStack.isEmpty()) {
			branchStack.peek().attMatched();
		}
		return patternBuilder.makeEmpty();
	}
	
	private Pattern unmatched() {
		return patternBuilder.makeNotAllowed();
	}
	
	private void pushChoiceBranch() {
		ChoiceBranch branch;
		if (branchStack.isEmpty()) {
			branch = new ChoiceBranch();
		} else {
			branch = branchStack.peek().createChildBranch();
		}
		branchStack.push(branch);
	}
	
	@Override
	public Object caseChoice(ChoicePattern p) {
		pushChoiceBranch();
		final Pattern derivative1 = (Pattern) p.getOperand1().apply(this);
		final int branch1MatchCount = branchStack.pop().getMatchCount();
		
		pushChoiceBranch();
		final Pattern derivative2 = (Pattern) p.getOperand2().apply(this);
		final int branch2MatchCount = branchStack.pop().getMatchCount();
		
		if (branch1MatchCount == branch2MatchCount) {
			return patternBuilder.makeChoice(derivative1, derivative2);
		} else if (branch1MatchCount > branch2MatchCount) {
			return derivative1;
		} else  {
			return derivative2;
		}
	}
		
	private static class ChoiceBranch {
		private int attMatches = 0;
		
		private List<ChoiceBranch> childBranches;
				
		void attMatched() {
			attMatches++;
		}
		
		int getMatchCount() {
			int matches = attMatches;
			if (childBranches != null) {
				for (ChoiceBranch childBranch : childBranches) {
					matches = Math.max(matches, childBranch.getMatchCount());
				}
			}
			return matches;
		}
		
		ChoiceBranch createChildBranch() {
			ChoiceBranch childBranch = new ChoiceBranch();
			if (childBranches == null) {
				childBranches = new LinkedList<ChoiceBranch>();
			}
			childBranches.add(childBranch);
			return childBranch;
		}
	}
} 