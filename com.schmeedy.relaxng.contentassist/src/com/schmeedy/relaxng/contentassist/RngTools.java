package com.schmeedy.relaxng.contentassist;

import com.schmeedy.relaxng.contentassist.internal.DefaultCompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.internal.DefaultRngResolver;
import com.schmeedy.relaxng.contentassist.internal.DefaultSchemaBinder;

public class RngTools {
	private static final RngTools INSTANCE = new RngTools();
	
	public static RngTools getInstance() {
		return INSTANCE;
	}
	
	private IRngResolver rngResolver;
	
	private ICompletionProposalCalculator completionProposalCalculator;
	
	private IRngSchemaBinder schemaBinder;
	
	private RngTools() {
		rngResolver = new DefaultRngResolver();
		completionProposalCalculator = new DefaultCompletionProposalCalculator(rngResolver);
		schemaBinder = DefaultSchemaBinder.getInstance();
	}
	
	public IRngResolver getRngResolver() {
		return rngResolver;
	}
	
	public ICompletionProposalCalculator getCompletionProposalCalculator() {
		return completionProposalCalculator;
	}
	
	public IRngSchemaBinder getSchemaBinder() {
		return schemaBinder;
	}
}
