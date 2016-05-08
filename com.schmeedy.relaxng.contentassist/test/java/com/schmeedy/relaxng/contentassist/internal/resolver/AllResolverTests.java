package com.schmeedy.relaxng.contentassist.internal.resolver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SimpleDocumentTest.class,
	DocumentWithXlinkTest.class,
	GeneratorConfigurationTest.class,
	Docbook5Test.class
})
public class AllResolverTests {
}