package com.schmeedy.relaxng.contentassist.internal.resolver;

import org.junit.Test;

import com.schmeedy.relaxng.contentassist.internal.resolver.test.AbstractInferenceTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.AttributeNamesAmongCompletionsTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementNamesAmongCompletionsTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementResolvableTest;


public class Docbook5Test extends AbstractInferenceTest {
	public Docbook5Test() {
		super("docbook5.rnc");
	}
	
	@Override
	protected INodeTest[] getNodeTests() {
		return new INodeTest[] {
				new ElementResolvableTest(getResolver()), 
				new AttributeNamesAmongCompletionsTest(getCompletionProposalCalculator()),
				new ElementNamesAmongCompletionsTest(getCompletionProposalCalculator())};
	}
	
	@Test
	public void testSampleDocbookArticle() {
		executeTestsOnDocument("sampleDocbookArticle.xml");
	}
	
	@Test
	public void testSampleDocbookBook1() {
		executeTestsOnDocument("sampleDocbookBook1.xml");
	}
	
	@Test
	public void testDocbook5TransitionGuide() {
		executeTestsOnDocument("docbook5TransitionGuide.xml");
	}
}
