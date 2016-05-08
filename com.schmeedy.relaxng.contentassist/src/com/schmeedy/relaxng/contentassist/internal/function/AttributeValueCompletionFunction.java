package com.schmeedy.relaxng.contentassist.internal.function;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.Pattern;
import org.w3c.dom.Attr;

public class AttributeValueCompletionFunction extends AbstractValueCompletionFunction {
	private QName contextAttributeName;
	
	public AttributeValueCompletionFunction(Attr contextAttribute, String[] ids) {
		super(ids);
		
		contextAttributeName = new QName(
				contextAttribute.getNamespaceURI(),
				contextAttribute.getLocalName());
	}
	
	@Override
	public Object caseAttribute(AttributePattern p) {
		if (p.getNameClass().contains(contextAttributeName)) {
			Pattern content = p.getContent();
			return content.apply(this);
		}
		return null;
	}
	
	@Override
	public Object caseGroup(GroupPattern p) {
		return caseBranchingPattern(p);
	}
}
