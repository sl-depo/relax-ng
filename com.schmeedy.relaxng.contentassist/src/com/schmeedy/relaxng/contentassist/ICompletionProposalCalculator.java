package com.schmeedy.relaxng.contentassist;

import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Main interface for clients. Implementations are capable of computing
 * completion proposals for DOM nodes of edited XML document based on RELAX NG
 * schema bound to that document.
 * 
 * In order to use <code>ICompletionProposalCalculator</code>, clients are
 * required to bind RELAX NG schema to the XML document using
 * <code>IRngSchemaBinder</code> before calling any method provided by this
 * interface. Failing to do so will result in
 * <code>UnresolvableElementException</code> being thrown.
 * 
 * @author Martin Schmied, schmeedy.com
 */
public interface ICompletionProposalCalculator {
	/**
	 * Computes possible attribute names for the given element. Note that
	 * attributes already present on the element affect the resulting proposals,
	 * but their names are not part of the returned set.
	 * 
	 * @param element
	 *            element whose attributes are being edited
	 * @return set of computed proposals, never null
	 */
	Set<IQNameCompletionProposal> getAttributeNameProposals(Element element);

	/**
	 * Computes possible values for the given attributes. Only attributes with
	 * enumeration-like or id-reference data types result in non-empty
	 * proposals. Additional SAX parse of the document is required in order to
	 * gather all possible IDs in case of an id-reference data type.
	 * 
	 * @param attribute
	 *            attribute whose value is being edited
	 * @param documentReader
	 *            Reader for the contents of the entire XML document, 
	 *            allowing for ID discovery
	 * @return set of computed proposals, never null
	 */
	Set<IValueCompletionProposal> getAttributeValueProposals(Attr attribute, Reader documentReader);

	/**
	 * Computes possible names for the given element. The current name of the
	 * given element is not taken into account, nor are the attributes currently
	 * specified on the element.
	 * 
	 * @param element
	 *            element whose name is being edited
	 * @return set of computed proposals, never null
	 */
	Set<IQNameCompletionProposal> getElementNameProposals(Element element);

	/**
	 * Computes possible names for the given element. The Text node is required
	 * for location reference.
	 * 
	 * @param unfinishedElement
	 *            text node representing the place in the document, where the
	 *            element is being inserted or edited but not yet transformed to
	 *            an element node
	 * @return set of computed proposals, never null
	 */
	Set<IQNameCompletionProposal> getElementNameProposals(Text unfinishedElement);

	/**
	 * Computes possible values of character content of the given element. Only
	 * elements with enumeration-like or id-reference data types result in
	 * non-empty proposals. Additional SAX parse of the document is required in
	 * order to gather all possible IDs in case of an id-reference data type.
	 * 
	 * @param element
	 *            element whose value is being edited
	 * @param documentReader
	 *            Reader for the contents of the entire XML document, 
	 *            allowing for ID discovery
	 * @return set of computed proposals, never null
	 */
	Set<IValueCompletionProposal> getElementValueProposals(Element element,	Reader documentReader);
}
