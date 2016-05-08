package com.schmeedy.relaxng.eclipse.core.internal.validator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.xml.core.internal.validation.core.AbstractNestedValidator;
import org.eclipse.wst.xml.core.internal.validation.core.NestedValidatorContext;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationInfo;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.schmeedy.relaxng.contentassist.IRngSchema;
import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;
import com.schmeedy.relaxng.eclipse.core.IRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.core.internal.DefaultRngSchemaResolver;
import com.schmeedy.relaxng.eclipse.core.internal.UriUtil;
import com.thaiopensource.relaxng.impl.JingValidationException;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.SAXSchemaReader;


@SuppressWarnings("restriction")
public class JingValidationDelegate extends AbstractNestedValidator {
	private IRngSchemaResolver schemaResolver = DefaultRngSchemaResolver.INSTANCE;
	
	private SoftReference<Map<IRngSchema, CachedValidationDriver>> validationDriversRef = 
		new SoftReference<Map<IRngSchema, CachedValidationDriver>>(new HashMap<IRngSchema, CachedValidationDriver>()); 
	
	@Override
	public ValidationReport validate(String uri, InputStream inputStream, NestedValidatorContext context) {
		ValidationInfo validationInfo = new ValidationInfo(uri);
		
		InputSource documentInputSource = new InputSource(inputStream);
		documentInputSource.setSystemId(uri);
				
		ValidationDriver validationDriver = getValidationDriver(uri, inputStream, validationInfo);
		
		if (validationDriver == null) {
			return validationInfo;
		}
		
		try {
			inputStream.reset(); // Some bytes from the document stream have already been read while retrieving schema
			validationDriver.validate(documentInputSource);
		} catch (Exception e) {
			return validationInfo;
		}
		
		return validationInfo;
	}

	@Override
	protected void addInfoToMessage(ValidationMessage validationMessage, IMessage message) {
		message.setAttribute(COLUMN_NUMBER_ATTRIBUTE, new Integer(validationMessage.getColumnNumber()));
	    
		if (validationMessage.getKey() == null) {
			message.setAttribute(SQUIGGLE_SELECTION_STRATEGY_ATTRIBUTE, "START_TAG");
		} else {
			String squiggleSelectionStrategy = getSquiggleSelectionStrategy(validationMessage.getKey());			
			if ("ATTRIBUTE_VALUE".equals(squiggleSelectionStrategy) || "TEXT".equals(squiggleSelectionStrategy)) {
				// fall back to START_TAG strategy as that's the only one that works nicely... 
				message.setAttribute(SQUIGGLE_SELECTION_STRATEGY_ATTRIBUTE, "START_TAG");
				return;
			}
			
			message.setAttribute(SQUIGGLE_SELECTION_STRATEGY_ATTRIBUTE, squiggleSelectionStrategy);
			if (validationMessage.getMessageArguments().length == 1) {
				String firstValidationMessageArgument = ((String) validationMessage.getMessageArguments()[0]).split(" ")[0];
				firstValidationMessageArgument = stripQuotes(firstValidationMessageArgument);
				message.setAttribute(SQUIGGLE_NAME_OR_VALUE_ATTRIBUTE, firstValidationMessageArgument);
			}
		}
	}
	
	private String stripQuotes(String string) {
		if (string.startsWith("\"")) {
			string = string.substring(1);
		}
		if (string.endsWith("\"")) {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}
	
	private String getSquiggleSelectionStrategy(String problemId) {
		if ("unknown_element".equals(problemId) ||
				"required_elements_missing".equals(problemId) ||
				"out_of_context_element".equals(problemId) ||
				"required_attributes_missing".equals(problemId) ||
				"unfinished_element".equals(problemId) ||
				"document_incomplete".equals(problemId)) {
			
			return "START_TAG";
		} else if ("impossible_attribute_ignored".equals(problemId)) {
			
			return "ATTRIBUTE_NAME";
		} else if ("bad_attribute_value".equals(problemId)) {
			
			return "ATTRIBUTE_VALUE";
		} else if ("text_not_allowed".equals(problemId) ||
				"string_not_allowed".equals(problemId) ||
				"only_text_not_allowed".equals(problemId)) {
			
			return "TEXT";
		}
		
		return "START_TAG";
	}

	private static class ReportingErrorHandler implements ErrorHandler {
		private ValidationInfo validationInfo;
		
		private String uri;
		
		public void init(ValidationInfo validationInfo, String uri) {
			this.validationInfo = validationInfo;
			this.uri = uri;
		}

		public void error(SAXParseException exception) throws SAXException {
			report(exception, IMessage.HIGH_SEVERITY);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			report(exception, IMessage.HIGH_SEVERITY);
		}

		public void warning(SAXParseException exception) throws SAXException {
			report(exception, IMessage.NORMAL_SEVERITY);
		}
		
		private void report(SAXParseException exception, int severity) {
			if (exception instanceof JingValidationException) {
				JingValidationException validationError = (JingValidationException) exception;
				validationInfo.addError(
						exception.getMessage(),
						exception.getLineNumber(),
						exception.getColumnNumber(),
						uri,
						validationError.getProblemId(),
						validationError.getMessageArg() == null ? new Object[]{} : new Object[]{validationError.getMessageArg()});
			} else {
				validationInfo.addError(
						exception.getMessage(),
						exception.getLineNumber(),
						exception.getColumnNumber(),
						uri);
			}
		}
	}
	
	// TODO : report errors while loading schema etc...
	private ValidationDriver getValidationDriver(String uri, InputStream documentInputStream, ValidationInfo validationInfo) {
		InputSource documentInputSource = new InputSource(documentInputStream);
		documentInputSource.setSystemId(uri);
		
		IRngSchema schema = schemaResolver.getSchema(documentInputSource);
		if (schema == null) {
			return null;
		}
		
		{ // try retrieving driver from cache
			CachedValidationDriver cachedValidationDriver = getCachedValidationDriver(schema);
			if (cachedValidationDriver != null) {
				cachedValidationDriver.initErrorHandler(validationInfo, uri);
				return cachedValidationDriver.getValidationDriver();
			}
		}
		
		SchemaReader schemaReader;
		if (schema.getSchemaSyntax() == RngSchemaSyntax.COMPACT) {
			schemaReader = CompactSchemaReader.getInstance();
		} else {
			schemaReader = SAXSchemaReader.getInstance();
		}
		
		ReportingErrorHandler errorHandler = new ReportingErrorHandler();
		errorHandler.init(validationInfo, uri);
		
		ValidationDriver validationDriver = 
			new ValidationDriver(
					new SinglePropertyMap(
							ValidateProperty.ERROR_HANDLER,
							errorHandler),
					schemaReader);
		
		
		try {
			if (!validationDriver.loadSchema(new InputSource(schema.openInputStream()))) {
				return null;
			}
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}

		{ // cache driver
			Map<IRngSchema, CachedValidationDriver> cache = validationDriversRef.get();
			if (cache == null) {
				cache = new HashMap<IRngSchema, CachedValidationDriver>();
				validationDriversRef = new SoftReference<Map<IRngSchema,CachedValidationDriver>>(cache);
			}
			cache.put(schema, new CachedValidationDriver(validationDriver, UriUtil.fetchFileInfo(schema.getSchemaUri()), errorHandler));
		}
		
		return validationDriver;
	}

	private CachedValidationDriver getCachedValidationDriver(IRngSchema schema) {
		Map<IRngSchema, CachedValidationDriver> cache = validationDriversRef.get();
		if (cache == null) {
			return null;
		}
		CachedValidationDriver cachedDriver = cache.get(schema);
		if (cachedDriver == null) {
			return null;
		}
		
		IFileInfo schemaFileInfo = UriUtil.fetchFileInfo(schema.getSchemaUri());
		if (cachedDriver.isStillValid(schemaFileInfo)) {
			return cachedDriver;
		} else {
			cache.remove(schema);
			return null;
		}
	}
	
	private static class CachedValidationDriver {
		private final ValidationDriver validationDriver;
		
		private final ReportingErrorHandler errorHandler;
		
		private final long schemaLastModified;

		public CachedValidationDriver(ValidationDriver validationDriver, IFileInfo schemaFileInfo, ReportingErrorHandler errorHandler) {
			super();
			this.validationDriver = validationDriver;
			this.schemaLastModified = schemaFileInfo.getLastModified();
			this.errorHandler = errorHandler;
		}
		
		public ValidationDriver getValidationDriver() {
			return validationDriver;
		}
		
		public boolean isStillValid(IFileInfo schemaFileInfo) {
			return schemaFileInfo.getLastModified() == schemaLastModified;
		}
		
		public void initErrorHandler(ValidationInfo validationInfo, String uri) {
			errorHandler.init(validationInfo, uri);
		}
	}
}