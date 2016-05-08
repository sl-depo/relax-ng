/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - implementation of org.eclipse.jface.text.contentassist.CompletionProposal
 *                       this class is modified version of CompletionProposal
 *     Martin Schmied  - lazy evaluation of the replacement string 
 *******************************************************************************/
package com.schmeedy.relaxng.eclipse.ui.internal.contentassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

class LazyCompletionProposal implements ICompletionProposal {
	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private IReplacementStringEvaluator replacementStringEvaluator;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;

	/**
	 * Creates a new completion proposal based on the provided information. The
	 * replacement string is considered being the display string too. All
	 * remaining fields are set to <code>null</code>.
	 * 
	 * @param iReplacementStringEvaluator
	 *            object for lazy evaluation of the replacement string
	 * @param replacementOffset
	 *            the offset of the text to be replaced
	 * @param replacementLength
	 *            the length of the text to be replaced
	 * @param cursorPosition
	 *            the position of the cursor following the insert relative to
	 *            replacementOffset
	 */
	public LazyCompletionProposal(IReplacementStringEvaluator iReplacementStringEvaluator, int replacementOffset, int replacementLength, int cursorPosition) {
		this(iReplacementStringEvaluator, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on
	 * the provided information.
	 * 
	 * @param replacementStringEvaluator
	 *            the actual string to be inserted into the document
	 * @param replacementOffset
	 *            the offset of the text to be replaced
	 * @param replacementLength
	 *            the length of the text to be replaced
	 * @param cursorPosition
	 *            the position of the cursor following the insert relative to
	 *            replacementOffset
	 * @param image
	 *            the image to display for this proposal
	 * @param displayString
	 *            the string to be displayed for the proposal
	 * @param contextInformation
	 *            the context information associated with this proposal
	 * @param additionalProposalInfo
	 *            the additional information associated with this proposal
	 */
	public LazyCompletionProposal(IReplacementStringEvaluator replacementStringEvaluator, int replacementOffset, int replacementLength, int cursorPosition,
			Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {

		Assert.isNotNull(replacementStringEvaluator);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		this.replacementStringEvaluator = replacementStringEvaluator;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		fContextInformation = contextInformation;
		fAdditionalProposalInfo = additionalProposalInfo;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, replacementStringEvaluator.getReplacement());
		} catch (BadLocationException x) {
			// ignore
		}
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fDisplayString;	
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}
	
	static interface IReplacementStringEvaluator {
		String getReplacement();
	}
}
