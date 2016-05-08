package com.schmeedy.relaxng.eclipse.core.internal.binding;

import java.util.Set;

public interface IRngSchemaBindingSet {
	Set<RngSchemaBinding> getBindings();
	
	public boolean contains(String namespace);
	
	public RngSchemaBinding get(String namespace);
}
