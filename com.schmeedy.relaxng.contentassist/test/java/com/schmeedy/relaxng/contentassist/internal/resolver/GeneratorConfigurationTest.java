package com.schmeedy.relaxng.contentassist.internal.resolver;

import org.junit.Test;

import com.schmeedy.relaxng.contentassist.internal.resolver.test.AbstractInferenceTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.AttributeNamesAmongCompletionsTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementNamesAmongCompletionsTest;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.ElementResolvableTest;


public class GeneratorConfigurationTest extends AbstractInferenceTest {
	public GeneratorConfigurationTest() {
		super("generatorConfiguration.rnc");
	}
	
	@Override
	protected INodeTest[] getNodeTests() {
		return new INodeTest[] {
				new ElementResolvableTest(getResolver()),
				new AttributeNamesAmongCompletionsTest(getCompletionProposalCalculator()),
				new ElementNamesAmongCompletionsTest(getCompletionProposalCalculator())};
	}
	
	@Test
	public void testGeneratorConfiguration() {
		executeTestsOnDocument("generatorConfiguration.xml");
	}
}
