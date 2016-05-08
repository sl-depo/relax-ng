package com.schmeedy.relaxng.contentassist.internal;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kohsuke.rngom.binary.Pattern;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.schmeedy.relaxng.contentassist.ICompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.IQNameCompletionProposal;
import com.schmeedy.relaxng.contentassist.IRngResolver;
import com.schmeedy.relaxng.contentassist.IValueCompletionProposal;
import com.schmeedy.relaxng.contentassist.internal.function.AttributeNameCompletionFunction;
import com.schmeedy.relaxng.contentassist.internal.function.AttributeValueCompletionFunction;
import com.schmeedy.relaxng.contentassist.internal.function.ElementNameCompletionFunction;
import com.schmeedy.relaxng.contentassist.internal.function.ElementValueCompletionFunction;
import com.schmeedy.relaxng.contentassist.internal.function.ProposedName;

public class DefaultCompletionProposalCalculator implements	ICompletionProposalCalculator {
	private IRngResolver resolver;
	private DefaultSchemaBinder schemaBinder = DefaultSchemaBinder.getInstance();
	
	public DefaultCompletionProposalCalculator(IRngResolver resolver) {
		super();
		this.resolver = resolver;
	}

	// @Override
	public Set<IQNameCompletionProposal> getAttributeNameProposals(Element element) {
		Pattern elementContentPattern = resolver.getRngPatternForContent(element);
		elementContentPattern = resolver.matchAttributes(element, elementContentPattern, null);
		ProposedName[] attNames = (ProposedName[]) elementContentPattern.apply(new AttributeNameCompletionFunction());
		Map<String, String> existingPrefixMapping = getNamespaceToPrefixMapping(element);
		
		return createProposals(attNames, existingPrefixMapping);
	}
	
	// @Override
	public Set<IValueCompletionProposal> getAttributeValueProposals(Attr attribute, Reader documentReader) {
		Pattern elementContentPattern = resolver.getRngPatternForContent(attribute.getOwnerElement());
		elementContentPattern = resolver.matchAttributes(attribute.getOwnerElement(), elementContentPattern, attribute);
		String[] ids = getDeclaredIds(attribute, documentReader);
		String[] values = (String[]) elementContentPattern.apply(new AttributeValueCompletionFunction(attribute, ids));
		
		return createProposals(values);
	}

	// @Override
	public Set<IQNameCompletionProposal> getElementNameProposals(Element element) {
		Pattern elementPattern = resolver.getRngPattern(element);
		ProposedName[] elementNames = (ProposedName[]) elementPattern.apply(new ElementNameCompletionFunction());
		Map<String, String> existingPrefixMapping = getNamespaceToPrefixMapping(element);
		
		return createProposals(elementNames, existingPrefixMapping);
	}
	
	// @Override
	public Set<IValueCompletionProposal> getElementValueProposals(Element element, Reader documentReader) {
		Pattern elementContentPattern = resolver.getRngPatternForContent(element);
		String[] ids = getDeclaredIds(element, documentReader);
		String[] values = (String[]) elementContentPattern.apply(new ElementValueCompletionFunction(ids));
		
		return createProposals(values);
	}

	// @Override
	public Set<IQNameCompletionProposal> getElementNameProposals(Text unfinishedElement) {
		Pattern elementPattern = resolver.getRngPattern(unfinishedElement);
		ProposedName[] elementNames = (ProposedName[]) elementPattern.apply(new ElementNameCompletionFunction());
		Map<String, String> existingPrefixMapping = getNamespaceToPrefixMapping(unfinishedElement);
		
		return createProposals(elementNames, existingPrefixMapping);
	}
	
	private String[] getDeclaredIds(Node node, Reader documentReader) {
		IdRefResolver idRefResolver = schemaBinder.getIdRefResolver(node);
		if (idRefResolver == null) {
			return new String[0];
		}
		return idRefResolver.getDeclaredIds(documentReader);
	}
	
	private Set<IValueCompletionProposal> createProposals(String[] values) {
		if (values != null) {
			Set<IValueCompletionProposal> proposals = new HashSet<IValueCompletionProposal>();
			for (int i = 0; i < values.length; i++) {
				proposals.add(new DefaultValueCompletionProposal(values[i]));
			}
			return proposals;
		}
		return Collections.emptySet();
	}
	
	private Set<IQNameCompletionProposal> createProposals(ProposedName[] names, Map<String, String> contextNsPrefixMapping) {
		if (names != null) {
			Set<IQNameCompletionProposal> proposals = new HashSet<IQNameCompletionProposal>();			
			Map<String, String> suggestedPrefixMapping = new HashMap<String, String>();
			for (int i = 0; i < names.length; i++) {
				proposals.add(new DefaultQNameCompletionProposal(names[i], contextNsPrefixMapping, suggestedPrefixMapping));
			}
			return proposals;
		}
		return Collections.emptySet();
	}
		
	private Map<String, String> getNamespaceToPrefixMapping(Node contextNode) {
		Map<String, String> mapping = new HashMap<String, String>();
		Node node = contextNode;
		while (node != null) {
			if (node instanceof Element) {
				Element el = (Element) node;
				NamedNodeMap atts = el.getAttributes();
				for (int i = 0; i < atts.getLength(); i++) {
					Attr att = (Attr) atts.item(i);
					if ("http://www.w3.org/2000/xmlns/".equals(att.getNamespaceURI())) {
						if (mapping.containsKey(att.getValue())) {
							continue;
						}
						if ("xmlns".equals(att.getLocalName())) {
							mapping.put(att.getValue(), "");
						} else if ("xmlns".equals(att.getPrefix())) {
							mapping.put(att.getValue(), att.getLocalName());
						}
					}
				}
			}
			node = node.getParentNode();
		}
		return mapping;
	}
}
 