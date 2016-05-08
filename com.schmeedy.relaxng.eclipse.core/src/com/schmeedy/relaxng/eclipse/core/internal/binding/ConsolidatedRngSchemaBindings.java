package com.schmeedy.relaxng.eclipse.core.internal.binding;

import java.util.HashSet;
import java.util.Set;

public enum ConsolidatedRngSchemaBindings implements IRngSchemaBindingSet {
	INSTANCE;
	
	private UserSchemaBindings userBindings = new UserSchemaBindings();
	
	private PluginSchemaBindings pluginBindings = PluginSchemaBindings.INSTANCE;
	
	// @Override
	public Set<RngSchemaBinding> getBindings() {
		Set<RngSchemaBinding> consolidated = new HashSet<RngSchemaBinding>();
		consolidated.addAll(userBindings.getBindings());
		consolidated.addAll(pluginBindings.getBindings());
		
		return consolidated;
	}
	
	// Override
	public boolean contains(String namespace) {
		return userBindings.contains(namespace) || pluginBindings.contains(namespace);
	}
	
	// Override
	public RngSchemaBinding get(String namespace) {
		RngSchemaBinding binding = userBindings.get(namespace);
		if (binding == null) {
			return pluginBindings.get(namespace);
		} else {
			return binding;
		}
	}
	
	public void reloadWorkingUserBindings() {
		userBindings.reload();
	}
}
