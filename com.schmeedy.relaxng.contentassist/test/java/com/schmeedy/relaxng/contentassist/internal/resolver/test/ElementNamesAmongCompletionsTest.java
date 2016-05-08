package com.schmeedy.relaxng.contentassist.internal.resolver.test;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.schmeedy.relaxng.contentassist.ICompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.IQNameCompletionProposal;
import com.schmeedy.relaxng.contentassist.internal.DefaultQNameCompletionProposal;
import com.schmeedy.relaxng.contentassist.internal.function.ProposedName;
import com.schmeedy.relaxng.contentassist.internal.resolver.test.AbstractInferenceTest.INodeTest;


public class ElementNamesAmongCompletionsTest implements INodeTest {
	private ICompletionProposalCalculator proposalCalc;
	
	public ElementNamesAmongCompletionsTest(ICompletionProposalCalculator proposalCalc) {
		super();
		this.proposalCalc = proposalCalc;
	}

	// @Override
	public boolean runOnNode(Node node) {
		return node instanceof Element;
	}
	
	// @Override
	public void execute(Node node) {
		Element element = (Element)node;
		Set<IQNameCompletionProposal> completionProposals = proposalCalc.getElementNameProposals(element);
		assertTrue("Element " + element + " not found among completion proposals.", completionProposals.contains(qName(element)));
	}
	
	private IQNameCompletionProposal qName(Node node) {
		return new DefaultQNameCompletionProposal(new ProposedName(node.getNamespaceURI(), node.getLocalName(), null, null), null, null);
	}
}
