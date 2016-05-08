package com.schmeedy.relaxng.eclipse.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class RngCorePlugin extends Plugin {
	// the shared instance
	private static RngCorePlugin plugin;
	
	public static RngCorePlugin getDefault() {
		return plugin;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
}
