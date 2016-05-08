package com.schmeedy.relaxng.contentassist.internal.resolver;

import org.junit.Test;

import com.schmeedy.relaxng.contentassist.internal.resolver.test.AbstractInferenceTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementNamesAmongCompletionsTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementResolvableTest;


public class SimpleDocumentTest extends AbstractInferenceTest {
	public SimpleDocumentTest() {
		super("simple1.rnc");
	}
	
	@Override
	protected INodeTest[] getNodeTests() {
		return new INodeTest[] {
				new ElementResolvableTest(getResolver()),
				new ElementNamesAmongCompletionsTest(getCompletionProposalCalculator())};
	}
	
	@Test
	public void testSimpleDocument() {
		executeTestsOnDocument("simple1.xml");
	}
}
