package com.schmeedy.relaxng.eclipse.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RngUiPlugin extends AbstractUIPlugin {	
	// The plug-in ID
	public static final String PLUGIN_ID = "cz.vse.xschm34.relax";

	// The shared instance
	private static RngUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public RngUiPlugin() {
	}

	public ImageDescriptor getIcon(String iconName) {
		IPath iconPath = new Path("icons/" + iconName);
		URL url = FileLocator.find(getBundle(), iconPath, null);

		if (url == null) {
			throw new IllegalArgumentException("Could not find the icon specified, full path: " + iconPath);
		}
			
		return ImageDescriptor.createFromURL(url);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RngUiPlugin getDefault() {
		return plugin;
	}

}
