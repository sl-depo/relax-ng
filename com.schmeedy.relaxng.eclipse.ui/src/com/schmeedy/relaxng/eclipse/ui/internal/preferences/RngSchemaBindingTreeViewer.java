/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM Corporation, Jens Lukowski/Innoopract - original implementation of 
 * 			the XMLCatalogTreeViewer
 * 		Martin Schmied - customizations for the RELAX NG Bindings preference page
 *******************************************************************************/
package com.schmeedy.relaxng.eclipse.ui.internal.preferences;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;
import com.schmeedy.relaxng.eclipse.core.internal.binding.IRngSchemaBindingSet;
import com.schmeedy.relaxng.eclipse.core.internal.binding.PluginSchemaBindings;
import com.schmeedy.relaxng.eclipse.core.internal.binding.RngSchemaBinding;
import com.schmeedy.relaxng.eclipse.core.internal.binding.UserSchemaBindings;
import com.schmeedy.relaxng.eclipse.ui.internal.RngUiPlugin;
import com.schmeedy.relaxng.eclipse.core.internal.UriUtil;

class RngSchemaBindingTreeViewer extends TreeViewer {
	static final String USER_SPECIFIED_ENTRIES_OBJECT = "User Specified Entries";
	
	static final String PLUGIN_SPECIFIED_ENTRIES_OBJECT = "Plugin Specified Entries";
	
	private static final ImageDescriptor ERROR_OVERLAY_DESCRIPTOR = RngUiPlugin.getDefault().getIcon("error_ovr.gif"); 
	
	private final Image bindingsImage = RngUiPlugin.getDefault().getIcon("chain.png").createImage();
	
	private UserSchemaBindings userBindings;
	
	private PluginSchemaBindings pluginBindings;
	
	private Map<Image, Image> contentTypeImgToErrorOverlayedImg = new HashMap<Image, Image>();

	RngSchemaBindingTreeViewer(Composite parent, UserSchemaBindings userBindings, PluginSchemaBindings pluginBindings) {
		super(parent, SWT.MULTI | SWT.BORDER);
		this.userBindings = userBindings;
		this.pluginBindings = pluginBindings;

		setContentProvider(new CatalogEntryContentProvider());
		setLabelProvider(new CatalogEntryLabelProvider());
	}

	private class CatalogEntryLabelProvider extends LabelProvider {
		private Map<RngSchemaSyntax, Image> imageTable = new EnumMap<RngSchemaSyntax, Image>(RngSchemaSyntax.class);

		@Override
		public String getText(Object object) {
			String result = null;
			if (object instanceof RngSchemaBinding) {
				RngSchemaBinding binding = (RngSchemaBinding) object;
				result = binding.getNamespace();
			}
			return result != null ? result : object.toString();
		}		
		
		@Override
		public Image getImage(Object object) {
			if (object instanceof String) {
				return bindingsImage;
			} else if (object instanceof RngSchemaBinding) {
				RngSchemaBinding bindingEntry = (RngSchemaBinding) object;
				
				Image icon = getIconForBinding(bindingEntry);
				
				if (!UriUtil.resourceExists(bindingEntry.getSchemaUri().toString())) {
					if (contentTypeImgToErrorOverlayedImg.containsKey(icon)) {
						return contentTypeImgToErrorOverlayedImg.get(icon);
					}
					
					Image decoratedIcon = new DecorationOverlayIcon(icon, ERROR_OVERLAY_DESCRIPTOR, IDecoration.BOTTOM_RIGHT).createImage();
					contentTypeImgToErrorOverlayedImg.put(icon, decoratedIcon);
					icon = decoratedIcon;
				}
				
				return icon;
			} else {
				throw new IllegalArgumentException("Unexpected item type.");
			}			 
		}

		private Image getIconForBinding(RngSchemaBinding bindingEntry) {
			if (imageTable.containsKey(bindingEntry.getSchemaSyntax())) {
				return imageTable.get(bindingEntry.getSchemaSyntax());
			}
			
			IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			ImageDescriptor imageDescriptor = editorRegistry.getImageDescriptor(bindingEntry.getSchemaUri().toString());
			
			Image image = imageDescriptor.createImage();
			imageTable.put(bindingEntry.getSchemaSyntax(), image);
							
			return image;
		}

		public void dispose() {
			super.dispose();
			for (Iterator<Image> it = imageTable.values().iterator(); it.hasNext();) {
				it.next().dispose();
			}
		}
	}


	public class CatalogEntryContentProvider implements ITreeContentProvider {
		protected Object[] roots;

		public CatalogEntryContentProvider() {
			roots = new Object[2];
			roots[0] = USER_SPECIFIED_ENTRIES_OBJECT;
			roots[1] = PLUGIN_SPECIFIED_ENTRIES_OBJECT;
		}

		public boolean isRoot(Object object) {
			return object instanceof String;
		}

		public Object[] getElements(Object element) {
			return roots;
		}

		public Object[] getChildren(Object parentElement) {
			Object[] result = new Object[0];
			if (parentElement == roots[0]) {
				result = getChildrenHelper(userBindings);
			}
			else if (parentElement == roots[1]) {
				result = getChildrenHelper(pluginBindings);
			}			
			return result;
		}

		private Object[] getChildrenHelper(IRngSchemaBindingSet bindingSet) {
			Set<RngSchemaBinding> entries = bindingSet.getBindings();
			List<RngSchemaBinding> sortedEntries; 
			if (entries.isEmpty()) {
				sortedEntries = Collections.emptyList();
			} else {
				Comparator<RngSchemaBinding> comparator = new Comparator<RngSchemaBinding>() {
					// @Override
					public int compare(RngSchemaBinding o1, RngSchemaBinding o2) {						
						return Collator.getInstance().compare(o1.getNamespace(), o2.getNamespace());
					}
				};
				sortedEntries = new LinkedList<RngSchemaBinding>(entries);
				Collections.sort(sortedEntries, comparator);
			}
			return sortedEntries.toArray();
		}

		public Object getParent(Object element) {
			return (element instanceof String) ? null : USER_SPECIFIED_ENTRIES_OBJECT;
		}

		public boolean hasChildren(Object element) {
			return isRoot(element) ? getChildren(element).length > 0 : false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
			bindingsImage.dispose();
			for (Image overlayedImage : contentTypeImgToErrorOverlayedImg.values()) {
				overlayedImage.dispose();
			}
		}
	}
}
