/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM Corporation, Jens Lukowski/Innoopract - original implementation of 
 * 			the XMLCatalogPreferencePage
 * 		Martin Schmied - customizations for the RELAX NG Bindings preference page
 *******************************************************************************/
package com.schmeedy.relaxng.eclipse.ui.internal.preferences;

import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.schmeedy.relaxng.eclipse.core.internal.binding.ConsolidatedRngSchemaBindings;
import com.schmeedy.relaxng.eclipse.core.internal.binding.PluginSchemaBindings;
import com.schmeedy.relaxng.eclipse.core.internal.binding.RngSchemaBinding;
import com.schmeedy.relaxng.eclipse.core.internal.binding.UserSchemaBindings;

public class RngSchemaBindingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private final UserSchemaBindings userBindings = new UserSchemaBindings();
	
	private final PluginSchemaBindings pluginBindings = PluginSchemaBindings.INSTANCE;
	
	private RngSchemaBindingView schemaBindingView;
	
	private RngBindingDetailsView bindingDetailsView;
	
	public RngSchemaBindingPreferencePage() {
		setDescription("Manage bindings between root element namespaces and RELAX NG schemata.");
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createSchemaBindingsView(composite);
		createBindingDetailsView(composite);
		
		return composite;
	}
	
	private void createSchemaBindingsView(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText("RELAX NG Schema Bindings");
		
		schemaBindingView = new RngSchemaBindingView(group, userBindings, pluginBindings);
		schemaBindingView.setLayoutData(new GridData(GridData.FILL_BOTH));
		schemaBindingView.addSelectionObserver(new RngSchemaBindingView.SimpleRngBindingSelectionObserver() {
			// @Override
			public void onSingleSelection(RngSchemaBinding binding) {
				bindingDetailsView.display(binding);
			}
			
			// @Override
			public void onMultiSelection(Set<RngSchemaBinding> bindings) {
				bindingDetailsView.clear();
			}
			
			// @Override
			public void onSelectionCleared() {
				bindingDetailsView.clear();
			}
		});
	}
	
	private void createBindingDetailsView(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Binding Details");
		
		bindingDetailsView = new RngBindingDetailsView(group);
	}

	// @Override
	public void init(IWorkbench workbench) {
		
	}
	
	@Override
	public boolean performCancel() {
		userBindings.reload();
		return true;
	}
	
	@Override
	public boolean performOk() {
		userBindings.save();
		ConsolidatedRngSchemaBindings.INSTANCE.reloadWorkingUserBindings();
		return true;
	}
}