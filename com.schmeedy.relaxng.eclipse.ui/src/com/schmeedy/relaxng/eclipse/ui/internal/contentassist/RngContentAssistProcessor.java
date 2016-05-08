package com.schmeedy.relaxng.eclipse.ui.internal.contentassist;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.schmeedy.relaxng.contentassist.ICompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.IQNameCompletionProposal;
import com.schmeedy.relaxng.contentassist.IRngSchema;
import com.schmeedy.relaxng.contentassist.IRngSchemaBinder;
import com.schmeedy.relaxng.contentassist.IValueCompletionProposal;
import com.schmeedy.relaxng.contentassist.InvalidRelaxNgSchemaException;
import com.schmeedy.relaxng.contentassist.RngTools;
import com.schmeedy.relaxng.eclipse.core.IRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.core.internal.DefaultRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.ui.internal.contentassist.LazyCompletionProposal.IReplacementStringEvaluator;


@SuppressWarnings("restriction")
public class RngContentAssistProcessor extends XMLContentAssistProcessor {
	private ICompletionProposalCalculator cpc = RngTools.getInstance().getCompletionProposalCalculator();
	
	private IRngSchemaResolver schemaResolver = DefaultRngSchemaResolver.INSTANCE;
	
	private IRngSchemaBinder schemaBinder = RngTools.getInstance().getSchemaBinder();
	
	private boolean hasBoundSchema(Document document) {
		if (!schemaBinder.hasBoundSchema(document)) {
			try {
				IRngSchema schema = schemaResolver.getSchema(document);
				if (schema == null) {
					return false;
				}
				schemaBinder.bind(document, schema);
			} catch (IOException e) {
				return false;
			} catch (InvalidRelaxNgSchemaException e) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {		
		IDOMNode contextNode = (IDOMNode) contentAssistRequest.getNode();

		if (!hasBoundSchema(contextNode.getOwnerDocument())) {
			return;
		}
		
		// Att name retrieval - Copy & Paste from AbstractContentAssistProcessor
		IStructuredDocumentRegion open = contextNode.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(contentAssistRequest.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}
		if (nameRegion == null) {
			return;
		}
		
		String attName = open.getText(nameRegion);
		Attr attNode = null;
		NamedNodeMap atts = contextNode.getAttributes();
		for (int j = 0; j < atts.getLength(); j++) {
			Attr att = (Attr) atts.item(j);
			String prefixedAttName = (att.getPrefix() == null ? "" : att.getPrefix() + ":") + att.getLocalName();
			if (prefixedAttName.equals(attName)) {
				attNode = att;
				break;
			}
		}
		
		if (attNode == null) {
			return;
		}
		
		Set<IValueCompletionProposal> proposals = cpc.getAttributeValueProposals(attNode, getDocumentContentAsStream(contentAssistRequest));
		
		for (IValueCompletionProposal proposal: sortValues(proposals)) {
			if (!proposal.getValue().startsWith(attNode.getValue())) {
				continue;
			}
			
			contentAssistRequest.addProposal(new CompletionProposal(
					"\"" + proposal.getValue() + "\"", 
					contentAssistRequest.getReplacementBeginPosition(),
					contentAssistRequest.getReplacementLength(),
					proposal.getValue().length() + 2,
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ENUM),
					proposal.getValue(),
					null,
					null));
		}
	}

	private Reader getDocumentContentAsStream(ContentAssistRequest contentAssistRequest) {
		String stringDocumentContent = contentAssistRequest.getDocumentRegion().getParentDocument().getText();
		return new StringReader(stringDocumentContent);
	}
	
	protected void addElementValueProposals(ContentAssistRequest contentAssistRequest) {
		IDOMNode contextNode = (IDOMNode) contentAssistRequest.getNode();

		// check preconditions
		if (!hasBoundSchema(contextNode.getOwnerDocument()) 
			|| !(contextNode instanceof Element)
			|| !(contextNode.getFirstChild() instanceof CharacterData)) {
			
			return;
		}
		
		String elementValue = ((CharacterData) contextNode.getFirstChild()).getData();
		
		if (elementValue == null) {
			elementValue = "";
		}
		
		Set<IValueCompletionProposal> proposals = cpc.getElementValueProposals((Element) contextNode, getDocumentContentAsStream(contentAssistRequest));
		
		for (IValueCompletionProposal proposal: sortValues(proposals)) {
			if (!proposal.getValue().startsWith(elementValue)) {
				continue;
			}
			
			contentAssistRequest.addProposal(new CompletionProposal(
					proposal.getValue(), 
					contentAssistRequest.getReplacementBeginPosition() - elementValue.length(),
					elementValue.length(),
					proposal.getValue().length(),
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ENUM),
					proposal.getValue(),
					null,
					null));
		}		
	}
	
	@Override
	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {		
		Node contextNode = contentAssistRequest.getNode();
		if (!hasBoundSchema(contextNode.getOwnerDocument())) {
			return;
		}
		
		if (!(contextNode instanceof Element)) {
			return;
		}
		
		Element contextElement = (Element) contextNode;
				
		Set<IQNameCompletionProposal> proposedNames = cpc.getAttributeNameProposals(contextElement);
		
		for (IQNameCompletionProposal proposal: sortNames(proposedNames)) {
			String completion = getPrefixedName(proposal);
					
			if (!completion.startsWith(contentAssistRequest.getMatchString()) && 
				!(proposal.isNSDeclarationRequired() && proposal.getLocalName().startsWith(contentAssistRequest.getMatchString()))) {
				continue;
			}
			
			if (proposal.isNSDeclarationRequired()) {
				completion = createNsDeclaration(proposal) + " " + completion;
			}
			
			String completionString = completion + "=\"\""; 
			
			Image icon = proposal.isMandatory() ?
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATT_REQ_OBJ) :
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
			
			contentAssistRequest.addProposal(new CompletionProposal(
					completionString, 
					contentAssistRequest.getReplacementBeginPosition(),
					contentAssistRequest.getReplacementLength(),
					completionString.length() - 1,
					icon,
					completion,
					null,
					proposal.getDocumentation()));
		}
	}

	private String getPrefixedName(IQNameCompletionProposal proposal) {
		return (proposal.getPrefix() == null || proposal.getPrefix() == "" ? "" : proposal.getPrefix() + ":") + proposal.getLocalName();
	}
	
	private void normalizeElementContent(Element element) {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Text && ((CharacterData) childNode).getLength() == 0) {
				element.removeChild(childNode);
			}
		}
	}
	
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
		if (contentAssistRequest.getNode() instanceof Element) {
			Element contextElement = (Element) contentAssistRequest.getNode();
			normalizeElementContent(contextElement);
			int numChildNodes = contextElement.getChildNodes().getLength();
			if (numChildNodes == 0 || (numChildNodes == 1 && contextElement.getFirstChild() instanceof CharacterData)) {
				if (numChildNodes == 0) {
					contextElement.appendChild(contextElement.getOwnerDocument().createTextNode(""));
				}
				addElementValueProposals(contentAssistRequest);
			}
		}
		addTagNameProposals(contentAssistRequest, childPosition);
	}
	
	@Override
	protected void addEndTagNameProposals(ContentAssistRequest contentAssistRequest) {
		// There's an issue with WTP when completing tag name just before an end tag of the parent
		// element. Then it's evaluated as end tag name completion...diagnose and correct this case.
		IStructuredDocumentRegion completionRegion = contentAssistRequest.getDocumentRegion();
		Node lastChildNode = contentAssistRequest.getNode().getLastChild();		
		if (completionRegion.getPrevious() != null && completionRegion.getPrevious().getText().matches("^<[^ >]+$")
				&& lastChildNode != null && lastChildNode instanceof Element) {
		
			Element contextElement = (Element) lastChildNode;
			contentAssistRequest.setDocumentRegion(completionRegion.getPrevious());
			contentAssistRequest.setNode(contextElement);
			String matchString = (contextElement.getPrefix() == null ? "" : contextElement.getPrefix() + ":") + contextElement.getLocalName();
			contentAssistRequest.setMatchString(matchString);
			contentAssistRequest.setParent(contextElement.getParentNode());
			contentAssistRequest.setReplacementBeginPosition(completionRegion.getPrevious().getStartOffset() + 1);
			contentAssistRequest.setReplacementLength(matchString.length());
			addTagNameProposals(contentAssistRequest, contextElement.getParentNode().getChildNodes().getLength() - 1);
		} else {		
			super.addEndTagNameProposals(contentAssistRequest);
		}
	}
	
	@Override
	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
		IDOMNode contextNode = (IDOMNode) contentAssistRequest.getNode();
		if (!hasBoundSchema(contextNode.getOwnerDocument())) {
			return;
		}
		
		if (contextNode instanceof Element &&
			contextNode.getLastStructuredDocumentRegion() != null &&	
			contextNode.getLastStructuredDocumentRegion().getStartOffset() == contentAssistRequest.getStartOffset()) {
			
			TextImpl mockText = (TextImpl) contextNode.getOwnerDocument().createTextNode("");
			((Element)contextNode).appendChild(mockText);
			contextNode = mockText;
		}
		
		Set<IQNameCompletionProposal> proposals = null;
		String completedString = contentAssistRequest.getMatchString();
		if (contextNode instanceof Element) {
			Element elementNode = (Element)contextNode;
			proposals = cpc.getElementNameProposals(elementNode);
		} else if (contextNode instanceof Text) {
			Text textNode = (Text)contextNode;			
			proposals = cpc.getElementNameProposals(textNode);
		} else {
			return;
		}
		
		final boolean rootElementCompletion = contextNode.getParentNode() instanceof Document;
		
		String completionTemplate;
		int currTagNameLength = -1;
		boolean tagInsertionMode = false;
		if (contextNode instanceof Text) {
			tagInsertionMode = !DOMRegionContext.XML_TAG_OPEN.equals(contentAssistRequest.getRegion().getType());
			if (tagInsertionMode) {
				completionTemplate = "<%s></%s>";
			} else {
				completionTemplate = "%s></%s>";
			}			
		} else {
			IStructuredDocumentRegion open = contextNode.getFirstStructuredDocumentRegion();
			ITextRegionList openRegions = open.getRegions();				
			int tagNameIdx = openRegions.indexOf(contentAssistRequest.getRegion());
			if (tagNameIdx + 1 == openRegions.size()) {
				completionTemplate = "%s></%s>";
			} else {
				String lastOpenRegion = open.getText(open.getLastRegion());
				if (">".equals(lastOpenRegion) || "/>".equals(lastOpenRegion)) {
					completionTemplate = "%s";
				} else {
					completionTemplate = "%s></%s>";					
				}
			}
			if (tagNameIdx != -1) {
				currTagNameLength = open.getText(openRegions.get(tagNameIdx)).length();
			}
		}
		
		for (IQNameCompletionProposal proposal: sortNames(proposals)) {
			String completedName = rootElementCompletion ? proposal.getLocalName() : getPrefixedName(proposal);
			
			if (!completedName.startsWith(completedString)&& 
				!(proposal.isNSDeclarationRequired() && proposal.getLocalName().startsWith(contentAssistRequest.getMatchString()))) {
				continue;
			}
			
			String completionString = completedName;
			
			if (rootElementCompletion) {
				if (proposal.getNamespaceURI() != null) {
					completionString += " xmlns=\"" + proposal.getNamespaceURI() + "\""; 
				}
			} else if (proposal.isNSDeclarationRequired()) {
				completionString += " " + createNsDeclaration(proposal);
			}			
			
			String displayString = completionString;			
			
			IReplacementStringEvaluator replacementEvaluator;
			if (contextNode instanceof Element && ((Element) contextNode).getAttributes().getLength() > 0) {
				replacementEvaluator = new StaticReplacementEvaluator(
						String.format(completionTemplate, completionString, completedName));				
			} else {	
				replacementEvaluator = new LazyElementNameReplacementEvaluator(
						String.format(completionTemplate, completionString + "%s", completedName),
						contextNode,
						proposal);
			}
			
			Image icon = proposal.isMandatory() ?
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC_EMPHASIZED) :
					XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
			
			contentAssistRequest.addProposal(new LazyCompletionProposal(
					replacementEvaluator, 
					contentAssistRequest.getReplacementBeginPosition(),
					Math.max(currTagNameLength, contentAssistRequest.getReplacementLength()),
					tagInsertionMode ? displayString.length() + 1 : displayString.length(),
					icon,
					displayString,
					null,
					proposal.getDocumentation()));
		}
	}

	private String createNsDeclaration(IQNameCompletionProposal proposal) {
		return "xmlns:" + proposal.getPrefix() + "=\"" + proposal.getNamespaceURI() + "\"";
	}
	
	private List<IValueCompletionProposal> sortValues(Set<IValueCompletionProposal> proposals) {
		List<IValueCompletionProposal> sorted = new ArrayList<IValueCompletionProposal>(proposals);
		Collections.sort(sorted, new Comparator<IValueCompletionProposal>() {
			public int compare(IValueCompletionProposal o1,	IValueCompletionProposal o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return sorted;
	}
	
	private List<IQNameCompletionProposal> sortNames(Set<IQNameCompletionProposal> proposals) {
		List<IQNameCompletionProposal> sorted = new ArrayList<IQNameCompletionProposal>(proposals);
		Collections.sort(sorted, new Comparator<IQNameCompletionProposal>() {
			public int compare(IQNameCompletionProposal o1, IQNameCompletionProposal o2) {
				if (o1.isMandatory() && !o2.isMandatory()) {
					return -1;
				} else if (o2.isMandatory() && !o1.isMandatory()) {
					return 1;
				}
				if (o1.getPrefix() == null) {
					if (o2.getPrefix() == null) {
						return o1.getLocalName().compareTo(o2.getLocalName()); 
					} else {
						return -1;
					}
				} else if (o2.getPrefix() == null) {
					return 1;
				}
				int comparison = o1.getPrefix().compareTo(o2.getPrefix());
				if (comparison == 0) {
					comparison = o1.getLocalName().compareTo(o2.getLocalName());
				}
				return comparison;
			}
		});
		return sorted;
	}
	
	private class StaticReplacementEvaluator implements IReplacementStringEvaluator {
		private String replacement;
		
		StaticReplacementEvaluator(String replacement) {
			this.replacement = replacement;
		}
		
		public String getReplacement() {
			return replacement;
		}
	}
	
	private class LazyElementNameReplacementEvaluator implements IReplacementStringEvaluator {
		private final String completionTemplate;
		
		private final Node contextNode;
		
		private final IQNameCompletionProposal elementProposal;
		
		LazyElementNameReplacementEvaluator(String completionTemplate, Node contextNode, IQNameCompletionProposal elementProposal) {
			this.completionTemplate = completionTemplate;
			this.contextNode = contextNode;
			this.elementProposal = elementProposal;
		}
		
		// @Override
		public String getReplacement() {
			return String.format(completionTemplate, getMandatoryAttributeString());
		}
		
		private String getMandatoryAttributeString() {
			Element mockElement = null;
			String attString = null;
			try {
				mockElement = contextNode.getOwnerDocument().createElementNS(elementProposal.getNamespaceURI(), elementProposal.getLocalName());
				contextNode.getParentNode().insertBefore(mockElement, contextNode);
				attString = getMandatoryAttributeString2(mockElement);
			} finally {
				if (mockElement != null && mockElement.getParentNode() != null) {
					mockElement.getParentNode().removeChild(mockElement);
				}
			}
			return attString == null ? "" : attString;
		}
		
		private String getMandatoryAttributeString2(Element contextElement) {
			Set<IQNameCompletionProposal> attProposals = cpc.getAttributeNameProposals(contextElement);
			String attString = "";
			Set<String> declaredNamespaces = new HashSet<String>();
			for (IQNameCompletionProposal attProposal : sortNames(attProposals)) {
				if (attProposal.isMandatory()) {
					attString += " " + getPrefixedName(attProposal) + "=\"\"";
				}
				if (attProposal.isNSDeclarationRequired() && !declaredNamespaces.contains(attProposal.getNamespaceURI())) {
					attString += " " + createNsDeclaration(attProposal);
					declaredNamespaces.add(attProposal.getNamespaceURI());
				}
			}
			return attString;
		}
	}
}