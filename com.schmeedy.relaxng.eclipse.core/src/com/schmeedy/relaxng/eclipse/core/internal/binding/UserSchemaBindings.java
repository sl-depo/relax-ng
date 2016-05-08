/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Schmied
 *     IBM Corporation, Jens Lukowski/Innoopract - TransformerFactory initialization
 *     
 *******************************************************************************/
package com.schmeedy.relaxng.eclipse.core.internal.binding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;
import com.schmeedy.relaxng.eclipse.core.internal.RngCorePlugin;

public class UserSchemaBindings implements IRngSchemaBindingSet {
	private static final String DEFAULT_STORE_FILE_NAME = "user-bindings.xml";
	
	private static final UserSchemaBindingPersistor PERSISTOR = new UserSchemaBindingPersistor();
	
	private List<BindingChangedListener> changeListeners = new LinkedList<BindingChangedListener>();
	
	private Map<String, RngSchemaBinding> namespaceToBindings = new HashMap<String, RngSchemaBinding>();
	
	private final File persistentStore; 
		
	public UserSchemaBindings() {
		IPath stateArea = RngCorePlugin.getDefault().getStateLocation();
		persistentStore = new File(stateArea.toFile(), DEFAULT_STORE_FILE_NAME);
		
		reload();
	}
	
	// @Override
	public boolean contains(String namespace) {
		return namespaceToBindings.containsKey(namespace);
	}
	
	// @Override
	public RngSchemaBinding get(String namespace) {
		return namespaceToBindings.get(namespace);
	}
	
	public void replaceBinding(RngSchemaBinding oldBinding, RngSchemaBinding replacement) {
		namespaceToBindings.remove(oldBinding.getNamespace());
		addBinding(replacement);
	}
	
	public void addBinding(RngSchemaBinding binding) {
		namespaceToBindings.put(binding.getNamespace(), binding);
		fireBindingChanged();
	}
	
	public void removeBinding(RngSchemaBinding binding) {
		namespaceToBindings.remove(binding.getNamespace());
		fireBindingChanged();
	}
	
	public void removeBindings(Set<RngSchemaBinding> removedBindings) {
		for (RngSchemaBinding rngSchemaBinding : removedBindings) {
			namespaceToBindings.remove(rngSchemaBinding.getNamespace());
		}
		fireBindingChanged();
	}

	public void save() {
		PERSISTOR.save(namespaceToBindings.values(), persistentStore);
	}
	
	public final void reload() {
		Set<RngSchemaBinding> bindings = PERSISTOR.load(persistentStore);
		namespaceToBindings.clear();
		for (RngSchemaBinding binding : bindings) {
			namespaceToBindings.put(binding.getNamespace(), binding);
		}
		fireBindingChanged();
	}
	
	// @Override
	public Set<RngSchemaBinding> getBindings() {
		return new HashSet<RngSchemaBinding>(namespaceToBindings.values());
	}
	
	public static interface BindingChangedListener {
		void onBindingChanged(UserSchemaBindings binding);
	}
	
	public void addChangeListener(BindingChangedListener changeListener) {
		changeListeners.add(changeListener);
	}
	
	public boolean removeChangeListener(BindingChangedListener changeListener) {
		return changeListeners.remove(changeListener);
	}
	
	private void fireBindingChanged() {
		for (BindingChangedListener changeListener : changeListeners) {
			changeListener.onBindingChanged(this);
		}
	}
	
	private static class UserSchemaBindingPersistor {
		private static final String ATTRIBUTE_SCHEMA_SYNTAX = "schema-syntax";

		private static final String ATTRIBUTE_URI = "schema-uri";

		private static final String ATTRIBUTE_NAMESPACE = "namespace";

		private static final String ELEMENT_SCHEMA_BINDING = "schema-binding";

		private static final String ELEMENT_BINDINGS = "bindings";

		private static final String NAMESPACE = "com.schmeedy.relaxng.eclipse.ui.schema-binding";
		
		private final DocumentBuilderFactory dbf;
		
		private Transformer transformer; 
		
		public UserSchemaBindingPersistor() {
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
			    transformer = transformerFactory.newTransformer();
			    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			    transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			    transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
				String encoding = "UTF-8"; // TBD //$NON-NLS-1$
				  
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			} catch (TransformerConfigurationException e) {
				throw new RuntimeException("Failed to configure transformer.", e);
			}
		}
		
		Set<RngSchemaBinding> load(File file) {
			if (file.exists()) {
				DocumentBuilder builder;
				try {
					builder = dbf.newDocumentBuilder();
					Document doc = builder.parse(file);
					return loadBindingsFromDocument(doc);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException("Could not configure document builder.", e);
				} catch (SAXException e) {
					throw new RuntimeException("Illegal XML configuration file.", e);
				} catch (IOException e) {
					throw new RuntimeException("Cannot read configuration file.", e);
				}
			} else {
				return new HashSet<RngSchemaBinding>();
			}
		}
		
		void save(Collection<RngSchemaBinding> bindings, File file) {
			try {
				DocumentBuilder docBuilder = dbf.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				Element bindingsElement = doc.createElementNS(NAMESPACE, ELEMENT_BINDINGS);
				doc.appendChild(bindingsElement);
				for (RngSchemaBinding rngSchemaBinding : bindings) {
					bindingsElement.appendChild(buildBinding(rngSchemaBinding, doc));
				}
				serialize(doc, new FileOutputStream(file));
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("Could not configure document builder.", e);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Illegal configuration storage (file).", e);
			} catch (TransformerException e) {
				throw new RuntimeException("Failed to create XML output for schema binding configuration file.", e);
			}
		}
		
		private Element buildBinding(RngSchemaBinding binding, Document doc) {
			Element bindingElement = doc.createElementNS(NAMESPACE, ELEMENT_SCHEMA_BINDING);
			bindingElement.setAttribute(ATTRIBUTE_NAMESPACE, binding.getNamespace());
			bindingElement.setAttribute(ATTRIBUTE_URI, binding.getSchemaUri().toString());
			bindingElement.setAttribute(ATTRIBUTE_SCHEMA_SYNTAX, binding.getSchemaSyntax().toString());
			return bindingElement;
		}
		
		private Set<RngSchemaBinding> loadBindingsFromDocument(Document document) {
			Element bindingsElement = document.getDocumentElement();
			NodeList bindings = bindingsElement.getElementsByTagNameNS(NAMESPACE, ELEMENT_SCHEMA_BINDING);
			Set<RngSchemaBinding> out = new HashSet<RngSchemaBinding>();
			for (int i = 0; i < bindings.getLength(); i++) {
				try {
					out.add(loadBinding((Element) bindings.item(i)));
				} catch (URISyntaxException e) {
					continue;
				}
			}
			return out;
		}
		
		private RngSchemaBinding loadBinding(Element bindingElement) throws URISyntaxException {
			String namespace = bindingElement.getAttribute(ATTRIBUTE_NAMESPACE);
			URI uri = new URI(bindingElement.getAttribute(ATTRIBUTE_URI));
			RngSchemaSyntax syntax = RngSchemaSyntax.valueOf(bindingElement.getAttribute(ATTRIBUTE_SCHEMA_SYNTAX));
			
			return new RngSchemaBinding(namespace, uri, syntax);
		}
		
		private void serialize(Document doc, OutputStream outputStream) throws TransformerException {
			transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
		}
	}
}
