package com.schmeedy.relaxng.contentassist.internal.resolver.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.schmeedy.relaxng.contentassist.IRngResolver;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.AbstractInferenceTest.INodeTest;


public final class ElementResolvableTest implements INodeTest {
	private IRngResolver resolver;

	public ElementResolvableTest(IRngResolver resolver) {
		this.resolver = resolver;
	}

	// @Override
	public boolean runOnNode(Node node) {
		return node instanceof Element;
	}

	// @Override
	public void execute(Node node) {
		Element element = (Element) node;
		Pattern pattern = resolver.getRngPatternForContent(element);
		assertNotNull(pattern);
		assertFalse("Element " + element + " resolved to NotAllowed.", pattern instanceof NotAllowedPattern);
	}
}