package com.schmeedy.relaxng.contentassist.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.visitor.PatternWalker;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.SimpleNameClass;
import org.relaxng.datatype.Datatype;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class IdRefResolver {
	private Map<QName, QName> elementToIdAttributeMap = new HashMap<QName, QName>();
	private XMLReader parser;
	
	public IdRefResolver(Pattern schemaPattern) {
		try {
			parser = XMLReaderFactory.createXMLReader();
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
		} catch (SAXException e) {
			throw new RuntimeException("Could not setup suitable parser.", e);
		}
		
		schemaPattern.accept(new IdAttributeBinder());
	}
	
	public String[] getDeclaredIds(Reader documentReader) {
		Set<String> idSet = new HashSet<String>();
		
		parser.setContentHandler(new IdFinder(idSet));
		try {
			parser.parse(new InputSource(documentReader));
		} catch (IOException e) {
			return idSet.toArray(new String[idSet.size()]);
		} catch (SAXException e) {
			return idSet.toArray(new String[idSet.size()]); 
		} 
		
		return idSet.toArray(new String[idSet.size()]);
	}
	
	private class IdFinder extends DefaultHandler {
		private Set<String> idSet;

		public IdFinder(Set<String> idSet) {
			super();
			this.idSet = idSet;
			
		}
	
		@Override
		public void startElement(String uri, String localName, String name,	Attributes attributes) throws SAXException {
			QName idAttributeName;
			if ((idAttributeName = elementToIdAttributeMap.get(qName(uri, localName))) != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					if (idAttributeName.equals(qName(attributes.getURI(i), attributes.getLocalName(i)))) {
						idSet.add(attributes.getValue(i));
					}
				}
			}
		}
		
		private QName qName(String uri, String localName) {
			if (uri == null) {
				return new QName(localName);
			} else {
				return new QName(uri, localName);
			}
		}
	}
	
	private class IdAttributeBinder extends PatternWalker {
		private QName contextElementName;
		
		private QName contextAttributeName;
		
		private Set<Pattern> visited = new HashSet<Pattern>();
		
		@Override
		public void visitElement(NameClass nc, Pattern content) {
			if (visited.contains(content)) {
				return;
			}
			if (nc instanceof SimpleNameClass) {
				contextElementName = ((SimpleNameClass) nc).name; 
			} else {
				contextElementName = null;
			}
			contextAttributeName = null;
			visited.add(content);
			super.visitElement(nc, content);
		}
		
		@Override
		public void visitAttribute(NameClass nc, Pattern value) {
			if (contextElementName != null && nc instanceof SimpleNameClass) {
				contextAttributeName = ((SimpleNameClass) nc).name;
			} else {
				contextAttributeName = null;
			}
			super.visitAttribute(nc, value);
		}
		
		@Override
		public void visitData(Datatype dt) {
			if (dt.getIdType() == Datatype.ID_TYPE_ID && contextAttributeName != null && contextElementName != null) {
				elementToIdAttributeMap.put(contextElementName, contextAttributeName);
			}
		}
	}	
}
