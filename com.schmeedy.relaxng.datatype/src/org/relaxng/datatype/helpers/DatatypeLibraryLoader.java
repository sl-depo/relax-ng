package org.relaxng.datatype.helpers;

import java.util.LinkedList;
import java.util.List;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

import com.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl;

public class DatatypeLibraryLoader implements DatatypeLibraryFactory {
	private static final String PLATFORM_SPECIFIC_DATATYPE_LIBRARY_FACTORY_CLASS = "org.relaxng.datatype.helpers.PlatformSpecificDatatypeLibraryFactory";

	private static final DatatypeLibraryFactoryImpl xsdDatatypeLibraryFactory = new DatatypeLibraryFactoryImpl();
	
	private static DatatypeLibraryFactory platformSpecificDatatypeLibraryFactory;
	
	static {
		try {
			Class<? extends DatatypeLibraryFactory> factoryClass = (Class<? extends DatatypeLibraryFactory>)
				DatatypeLibraryLoader.class.getClassLoader().loadClass(PLATFORM_SPECIFIC_DATATYPE_LIBRARY_FACTORY_CLASS);
			platformSpecificDatatypeLibraryFactory = factoryClass.newInstance();
		} catch (Throwable t) {}
	}
	
	public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
		List<DatatypeLibraryFactory> libraryFactories = new LinkedList<DatatypeLibraryFactory>();
		if (platformSpecificDatatypeLibraryFactory != null) {
			libraryFactories.add(platformSpecificDatatypeLibraryFactory);
		}
		libraryFactories.add(xsdDatatypeLibraryFactory);
		
		for (DatatypeLibraryFactory datatypeLibraryFactory : libraryFactories) {
			DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(namespaceURI);
			if (dl != null) {
				return dl;
			}
		}
		
		throw new IllegalArgumentException("Unknown datatype namespace: " + namespaceURI);
	}
}

