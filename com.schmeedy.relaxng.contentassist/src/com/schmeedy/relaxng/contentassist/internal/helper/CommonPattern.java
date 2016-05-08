package com.schmeedy.relaxng.contentassist.internal.helper;

import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.Pattern;

public class CommonPattern {
	public static final Pattern NOT_ALLOWED_PATTERN = new NotAllowedPattern();
	public static final Pattern EMPTY_PATTERN = new EmptyPattern();
}
