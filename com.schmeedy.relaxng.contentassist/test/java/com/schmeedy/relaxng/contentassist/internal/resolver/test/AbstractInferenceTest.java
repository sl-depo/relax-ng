package com.schmeedy.relaxng.contentassist.internal.resolver.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.SchemaBuilderImpl;
import org.kohsuke.rngom.binary.SchemaPatternBuilder;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.schmeedy.relaxng.contentassist.ICompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.IRngResolver;
import com.schmeedy.relaxng.contentassist.internal.DefaultCompletionProposalCalculator;
import com.schmeedy.relaxng.contentassist.internal.DefaultRngResolver;
import com.schmeedy.relaxng.contentassist.internal.resolver.DefaultErrorHandler;



public abstract class AbstractInferenceTest {
	private static final String RESOURCE_DIR = "/resolver-tests/";

	private static DocumentBuilder builder;
	
	static {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builderFactory.setValidating(false);
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
		
	private IRngResolver resolver;
	private ICompletionProposalCalculator proposalCalculator;

	private Pattern documentSchema;
	
	protected AbstractInferenceTest(String rncSchema) {
		resolver = new DefaultRngResolver();
		proposalCalculator = new DefaultCompletionProposalCalculator(resolver);
		try {
			init(rncSchema);
		} catch (Exception e) {
			throw new RuntimeException("Error when initializing test.", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void init(String rncSchema) throws Exception {
		InputSource schemaInputSource = new InputSource(AbstractInferenceTest.class.getResourceAsStream(RESOURCE_DIR + rncSchema));
		Parseable parseable = new CompactParseable(schemaInputSource, new DefaultErrorHandler());
		documentSchema = (Pattern) parseable.parse(
				new SchemaBuilderImpl(
						new DefaultErrorHandler(),
						new BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader()),
						new SchemaPatternBuilder()
					));
	}
	
	protected void executeTestsOnDocument(String xmlDocument) {
		Document document;
		try {
			InputSource documentInputSource = new InputSource(AbstractInferenceTest.class.getResourceAsStream(RESOURCE_DIR + xmlDocument));
			document = builder.parse(documentInputSource);
			document.setUserData(IRngResolver.KEY_RNG_PATTERN, documentSchema, null);
		} catch (Exception e) {
			throw new RuntimeException("Error loading document.", e);
		}
		INodeTest[] nodeTests = getNodeTests();
		for (int i = 0; i < nodeTests.length; i++) {
			executeNodeTest(nodeTests[i], document);
		}
	}
	
	protected IRngResolver getResolver() {
		return resolver;
	}
	
	protected ICompletionProposalCalculator getCompletionProposalCalculator() {
		return proposalCalculator;
	}
	
	protected abstract INodeTest[] getNodeTests();	
	
	private void executeNodeTest(INodeTest test, Document document) {
		if (test.runOnNode(document)) {
			test.execute(document);
		}
		executeNodeTestRecursive(test, document);
	}
	
	private void executeNodeTestRecursive(INodeTest test, Node parentNode) {
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (test.runOnNode(node)) {
				test.execute(node);
			}
			executeNodeTestRecursive(test, node);
		}
	}
	
	public interface INodeTest {
		boolean runOnNode(Node node);
		void execute(Node node);
	}
}
