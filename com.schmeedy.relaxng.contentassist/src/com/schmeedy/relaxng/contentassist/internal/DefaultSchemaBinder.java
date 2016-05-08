package com.schmeedy.relaxng.contentassist.internal;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.SchemaBuilderImpl;
import org.kohsuke.rngom.binary.SchemaPatternBuilder;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.schmeedy.relaxng.contentassist.IRngResolver;
import com.schmeedy.relaxng.contentassist.IRngSchema;
import com.schmeedy.relaxng.contentassist.IRngSchemaBinder;
import com.schmeedy.relaxng.contentassist.InvalidRelaxNgSchemaException;
import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;


public class DefaultSchemaBinder implements IRngSchemaBinder {
	private static final DefaultSchemaBinder INSTANCE = new DefaultSchemaBinder();
	
	public static DefaultSchemaBinder getInstance() {
		return INSTANCE;
	}
	
	private Map<Document, IdRefResolver> idRefResolvers = new WeakHashMap<Document, IdRefResolver>();
	
	
	@SuppressWarnings("unchecked")
	// @Override
	public void bind(Document document, IRngSchema schema) throws IOException, InvalidRelaxNgSchemaException {
		try {
			InputSource schemaInputSource = new InputSource(schema.openInputStream());
			
			Parseable parseable = null;
			ErrorHandler errorHandler = new DefaultErrorHandler();
			if (schema.getSchemaSyntax() == RngSchemaSyntax.COMPACT) {
				parseable = new CompactParseable(schemaInputSource, errorHandler);
			} else {
				parseable = new SAXParseable(schemaInputSource, errorHandler);
			}

			Pattern schemaPattern = (Pattern) parseable.parse(
				new SchemaBuilderImpl(
					errorHandler,
					new BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader()),
					new SchemaPatternBuilder()
				));
			
			document.setUserData(IRngResolver.KEY_RNG_PATTERN, schemaPattern, null);
			idRefResolvers.put(document, new IdRefResolver(schemaPattern));
		} catch (BuildException e) {
			throw new IOException("Failed to load RELAX NG schema, cause: " + e.getMessage());
		} catch (IllegalSchemaException e) {
			throw new InvalidRelaxNgSchemaException("Schema violates the RELAX NG grammar.", e);
		}
	}
	
	public void unBind(Document document) {
		document.setUserData(IRngResolver.KEY_RNG_PATTERN, null, null);		
	}
	
	// @Override
	public boolean hasBoundSchema(Document document) {
		return document.getUserData(IRngResolver.KEY_RNG_PATTERN) != null;
	}
		
	public IdRefResolver getIdRefResolver(Node node) {
		Document document = node.getOwnerDocument();
		if (document == null) {
			return null;
		}
		return idRefResolvers.get(document);
	}
	
	private static class DefaultErrorHandler implements ErrorHandler {
		private static final Log logger = LogFactory.getLog(DefaultErrorHandler.class);
		
		// @Override
		public void error(SAXParseException exception) throws SAXException {
			throw exception; 
		}

		// @Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

		// @Override
		public void warning(SAXParseException exception) throws SAXException {
			logger.warn("SAX Warning: " + exception.getMessage());
		}

	}
}
