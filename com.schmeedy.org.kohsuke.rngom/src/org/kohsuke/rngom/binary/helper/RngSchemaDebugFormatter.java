package org.kohsuke.rngom.binary.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.BinaryPattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.TextPattern;
import org.kohsuke.rngom.binary.ValuePattern;
import org.kohsuke.rngom.nc.SimpleNameClass;

public class RngSchemaDebugFormatter {
	
	private static String indent(String str, char indentChar, int indentation) {
		String out = str;
		String indentString = "";
		for (int i = 0; i < indentation; i++) {
			indentString += indentChar;
		}
		
		out = indentString + out;
		out = out.replaceAll("\\n", "\n" + indentString);
		if (out.charAt(out.length() - 1) == '\t') {
			out = out.substring(0, out.length() - 2);
		}
		return out;
	}
	
	private static String demarkGroup(String str) {
		String out = "(\n" + indent(str, ' ', 2);
		if (!str.endsWith("\n")) {
			out += "\n";
		}
		out += ")";
		return out;
	}
	
	private static String formatBinaryPattern(BinaryPattern p) {
		String delimiter;
		if (p instanceof GroupPattern) {
			delimiter = " ,";
		} else if (p instanceof ChoicePattern) {
			delimiter = " |";
		} else if (p instanceof InterleavePattern) {
			delimiter = " &";
		} else {
			delimiter = "!after!";
		}
		
		// Pull all members up
		List<Pattern> groupMembers = new ArrayList<Pattern>();
		BinaryPattern bp = p;
		while (bp.getOperand1().getClass().equals(p.getClass())) {
			groupMembers.add(bp.getOperand2());
			bp = (BinaryPattern)bp.getOperand1();
		}
		groupMembers.add(bp.getOperand2());
		groupMembers.add(bp.getOperand1());
		Collections.reverse(groupMembers);
		
		StringBuffer out = new StringBuffer();
		boolean firstLoop = true;
		for (Pattern member: groupMembers) {
			if (!firstLoop) {
				out.append(delimiter + "\n");
			}
			out.append(format(member));
			firstLoop = false;
		}
		
		return demarkGroup(out.toString());
	}
	
	public static String format(Pattern pattern) {
		if (pattern instanceof ElementPattern) {
			ElementPattern p = (ElementPattern) pattern;
			QName elementName = ((SimpleNameClass)p.getNameClass()).name;
			String out = "element " + elementName.getNamespaceURI() + ":" + elementName.getLocalPart();
			if (p.getContent() instanceof ElementPattern || p.getContent() instanceof BinaryPattern || p.getContent() instanceof OneOrMorePattern) {
				return out + " {\n" + formatIndent(p.getContent()) + "\n}";
			} else {
				return out + " {" + format(p.getContent()) + "}";
			}
		} else if (pattern instanceof AttributePattern) {
			AttributePattern p = (AttributePattern) pattern;
			QName attName = ((SimpleNameClass)p.getNameClass()).name;
			String value = format(p.getContent());			
			return "attribute " + attName.getLocalPart() + " {" + value + "}";
		} else if (pattern instanceof OneOrMorePattern) {
			OneOrMorePattern p = (OneOrMorePattern) pattern;
			return format(p.getOperand()) + "+";
		} else if (pattern instanceof RefPattern) {
			return format(((RefPattern) pattern).getPattern());
		} else if (pattern instanceof EmptyPattern) {
			return "<<empty>>" ;			
		} else if (pattern instanceof NotAllowedPattern) {
			return "<<not-allowed>>"; 
		} else if (pattern instanceof BinaryPattern) {
			return formatBinaryPattern((BinaryPattern) pattern);
		} else if (pattern instanceof TextPattern) {
			return "text";
		} else if (pattern instanceof ValuePattern) {
			return "\"" + ((ValuePattern)pattern).getValue() + "\"";
		} else {
			return pattern.toString();
		}
	}
	
	public static String formatIndent(Pattern pattern) {
		return indent(format(pattern), '\t', 1);
	}
}
