package com.schmeedy.relaxng.eclipse.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.schmeedy.relaxng.contentassist.IRngSchema;
import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;
import com.schmeedy.relaxng.eclipse.core.IRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.core.internal.binding.BindingUtils;
import com.schmeedy.relaxng.eclipse.core.internal.binding.ConsolidatedRngSchemaBindings;
import com.schmeedy.relaxng.eclipse.core.internal.binding.IRngSchemaBindingSet;
import com.schmeedy.relaxng.eclipse.core.internal.binding.RngSchemaBinding;

public enum DefaultRngSchemaResolver implements IRngSchemaResolver {
	INSTANCE;
	
	private IRngSchemaBindingSet configuredBindings = ConsolidatedRngSchemaBindings.INSTANCE;
	
	private XmlStartHandler xmlStartHandler = new XmlStartHandler();
	
	public IRngSchema getSchema(InputSource documentInputSource) {
		try {
			xmlStartHandler.parse(documentInputSource);
			{	
				Map<String, String> piParams = xmlStartHandler.getPiParams();
				if (piParams != null) {
					IRngSchema schema = getSchemaFromProcessingInstructionParams(piParams);
					if (schema != null) {
						return schema;
					}
				}
			}
			{
				if (xmlStartHandler.getRootElementNamespace() != null) {
					return getSchemaForNamespace(xmlStartHandler.getRootElementNamespace());
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			return null;
		}		
	}
	
	public IRngSchema getSchema(Document document) {
		{
			Map<String, String> piParamMap = extractProcessingInstructionParams(document);
			if (piParamMap != null) {
				IRngSchema schema = getSchemaFromProcessingInstructionParams(piParamMap);
				if (schema != null) {
					return schema;
				}
			}
		}	
		{
			Element docElement = document.getDocumentElement();
			if (docElement == null || docElement.getNamespaceURI() == null) {
				return null;
			}
			return getSchemaForNamespace(docElement.getNamespaceURI());
		}
	}
	
	private IRngSchema getSchemaForNamespace(String namespace) {
		if (configuredBindings.contains(namespace)) {
			return new BoundRngSchema(configuredBindings.get(namespace));
		} else {
			return null;
		}
	}
	
	private Map<String, String> extractProcessingInstructionParams(Document document) {
		NodeList childNodes = document.getChildNodes();
		List<Map<String, String>> partialMatches = new LinkedList<Map<String, String>>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i) instanceof ProcessingInstruction) {
				ProcessingInstruction pi = (ProcessingInstruction) childNodes.item(i);
				if (!RngConstants.BINDING_INSTRUCTION_TARGET.equals(pi.getTarget())) {
					continue;
				}
				Map<String, String> paramMap = BindingUtils.parseBindingProcessingInstructionData(pi.getData());
				String mode = paramMap.get(RngConstants.BINDING_INSTRUCTION_MODE_PSEUDO_ATT_NAME); 
				if (mode == null || !RngConstants.BINDING_INSTRUCTION_MODE_PSEUDO_ATT_VALUE.equals(mode)) {
					partialMatches.add(paramMap);
				} else {
					return paramMap;
				}
			}
			if (childNodes.item(i) instanceof Element) {
				break;
			}
		}
		return chooseBestPiMatch(partialMatches);
	}

	private Map<String, String> chooseBestPiMatch(List<Map<String, String>> partialMatches) {
		for (Map<String, String> pMap : partialMatches) {
			String schemaUri = pMap.get(RngConstants.BINDING_INSTRUCTION_URI_PSEUDO_ATT_NAME);
			if (schemaUri != null && UriUtil.resourceExists(schemaUri)) {
				return pMap;
			}
		}
		return null;
	}
	
	private IRngSchema getSchemaFromProcessingInstructionParams(Map<String, String> paramMap) {
		String schemaUri = paramMap.get(RngConstants.BINDING_INSTRUCTION_URI_PSEUDO_ATT_NAME);
		String mimeType = paramMap.get(RngConstants.BINDING_INSTRUCTION_TYPE_PSEUDO_ATT_NAME);
		if (schemaUri == null || mimeType == null || !UriUtil.resourceExists(schemaUri)) {
			return null;
		}
		RngSchemaSyntax syntax = BindingUtils.convertMimeTypeToSchemaSyntax(mimeType);
		if (syntax == null) {
			syntax = BindingUtils.guessSchemaSyntax(schemaUri);
			if (syntax == null) {
				return null;
			}
		}
		return new EfsRngSchema(UriUtil.resolveUri(schemaUri), syntax);
	}
	
	private static class EfsRngSchema implements IRngSchema {
		private final URI schemaUri;
		
		private final RngSchemaSyntax schemaSyntax;
		
		public EfsRngSchema(URI schemaUri, RngSchemaSyntax schemaSyntax) {
			super();
			this.schemaUri = schemaUri;
			this.schemaSyntax = schemaSyntax;
		}

		// @Override
		public RngSchemaSyntax getSchemaSyntax() {
			return schemaSyntax;
		}

		// @Override
		public InputStream openInputStream() throws IOException {
			return UriUtil.openResource(schemaUri);
		}
		
		// @Override
		public URI getSchemaUri() {
			return schemaUri;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schemaUri == null) ? 0 : schemaUri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EfsRngSchema other = (EfsRngSchema) obj;
			if (schemaUri == null) {
				if (other.schemaUri != null)
					return false;
			} else if (!schemaUri.equals(other.schemaUri))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return schemaUri.toString();
		}		
	}
	
	private static class BoundRngSchema implements IRngSchema {
		private RngSchemaBinding binding;
		
		BoundRngSchema(RngSchemaBinding binding) {
			this.binding = binding;
		}
		
		// @Override
		public RngSchemaSyntax getSchemaSyntax() {
			return binding.getSchemaSyntax();
		}
		
		// @Override
		public InputStream openInputStream() throws IOException {
			return UriUtil.openResource(binding.getSchemaUri().toString());
		}
		
		// @Override
		public URI getSchemaUri() {
			return binding.getSchemaUri();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((binding == null) ? 0 : binding.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BoundRngSchema other = (BoundRngSchema) obj;
			if (binding == null) {
				if (other.binding != null)
					return false;
			} else if (!binding.equals(other.binding))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return binding.getSchemaUri().toString();
		}
	}
	
	private class XmlStartHandler extends DefaultHandler {
		private XMLReader reader;
		
		private List<Map<String, String>> partialPiMatches = new LinkedList<Map<String,String>>();
		
		private Map<String, String> exactPiMatch;
		
		private String rootElementNamespace;
		
		XmlStartHandler() {
			try {
				reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(this);
			} catch (SAXException e) {
				// TODO : log...is there a way to recover???
			}			
		}
		
		void parse(InputSource inputSource) throws IOException, SAXException {
			exactPiMatch = null;
			partialPiMatches.clear();
			rootElementNamespace = null;
			try {
				reader.parse(inputSource);
			} catch (SAXException e) {
				// Stopping parse, want to handle document that are NOT well-formed as well...
			}
		}
		
		String getRootElementNamespace() {
			return rootElementNamespace;
		}
		
		Map<String, String> getPiParams() {
			if (exactPiMatch != null) {
				return exactPiMatch;
			} else {
				return chooseBestPiMatch(partialPiMatches);
			}
		}
		
		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			if (RngConstants.BINDING_INSTRUCTION_TARGET.equals(target)) {
				Map<String, String> paramMap = BindingUtils.parseBindingProcessingInstructionData(data);
				if (paramMap.containsKey(RngConstants.BINDING_INSTRUCTION_MODE_PSEUDO_ATT_NAME) &&
						RngConstants.BINDING_INSTRUCTION_MODE_PSEUDO_ATT_VALUE.equals(paramMap.get(RngConstants.BINDING_INSTRUCTION_MODE_PSEUDO_ATT_NAME))) {
					exactPiMatch = paramMap;
				} else {
					partialPiMatches.add(paramMap);
				}
			}
		}
		
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			rootElementNamespace = uri;
			throw new StopParsingException();
		}
		
	}
	
	private static class StopParsingException extends SAXException {
		private static final long serialVersionUID = 1L;
	}
}
