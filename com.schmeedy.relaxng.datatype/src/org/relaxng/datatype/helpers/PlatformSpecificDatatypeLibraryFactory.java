package org.relaxng.datatype.helpers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class PlatformSpecificDatatypeLibraryFactory implements DatatypeLibraryFactory {
	private static final String EXTENSION_POINT_ID = "com.schmeedy.relaxng.datatype.datatypeLibrary";
	
	private Map<String, DatatypeLibrary> contributedLibraries = new HashMap<String, DatatypeLibrary>();
	
	public PlatformSpecificDatatypeLibraryFactory() {
		for (IConfigurationElement datatypeLibrary: Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			try {
				String namespace = datatypeLibrary.getAttribute("namespace-uri").trim();
				DatatypeLibrary dl = (DatatypeLibrary) datatypeLibrary.createExecutableExtension("library-class");
				contributedLibraries.put(namespace, dl);
			} catch (Exception e) {
				continue;
			}			
		}
	}
	
	public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
		if (contributedLibraries.containsKey(namespaceURI)) {
			return contributedLibraries.get(namespaceURI);
		}
		return null;
	}

}
