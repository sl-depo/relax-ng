/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM Corporation, Jens Lukowski/Innoopract - original implementation of 
 * 			the EditCatalogEntryDialog
 * 		Martin Schmied - customizations for the RELAX NG Bindings preference page
 *******************************************************************************/
package com.schmeedy.relaxng.eclipse.ui.internal.preferences;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.ui.internal.dialogs.SelectSingleFileDialog;

import com.schmeedy.relaxng.contentassist.IRngSchema.RngSchemaSyntax;
import com.schmeedy.relaxng.eclipse.core.internal.binding.RngSchemaBinding;
import com.schmeedy.relaxng.eclipse.core.internal.binding.UserSchemaBindings;
import com.schmeedy.relaxng.eclipse.ui.internal.RngUiPlugin;
import com.schmeedy.relaxng.eclipse.core.internal.UriUtil;

public class EditRngSchemaBindingDialog extends Dialog {
	private static final int SCHEMA_TYPE_COMPACT_COMBO_IDX = 1;
	private static final int SCHEMA_TYPE_XML_COMBO_IDX = 0;
	
	private UserSchemaBindings userBindings;
	private RngSchemaBinding binding;
	private Text namespaceText;
	private Text schemaLocationText;	
	private Combo schemaTypeCombo;
	private Label errorReportArea;
	
	private Color errorReportColor;
	private Color warningReportColor;
	private Image browseImage;
	private Button okButton;
	
	private EditMode editMode;

	/**
	 * Form for a new binding
	 * 
	 * @param parentShell
	 * @param userBindings
	 */
	public EditRngSchemaBindingDialog(Shell parentShell, UserSchemaBindings userBindings) {
		this(parentShell, userBindings, EditMode.INSERT);
	}
	
	/**
	 * Form for updating of an existing binding
	 * 
	 * @param parentShell
	 * @param binding
	 */
	public EditRngSchemaBindingDialog(Shell parentShell, UserSchemaBindings userBindings, RngSchemaBinding binding) {
		this(parentShell, userBindings, EditMode.UPDATE);
		this.binding = binding;
	}

	private EditRngSchemaBindingDialog(Shell parentShell, UserSchemaBindings userBindings , EditMode editMode) {
		super(parentShell);
		this.editMode = editMode;
		this.userBindings = userBindings;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		errorReportColor = new Color(parent.getDisplay(), 200, 0, 0);
		warningReportColor = new Color(parent.getDisplay(), 204, 102, 0);
		browseImage = RngUiPlugin.getDefault().getIcon("file_expand.gif").createImage();
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.createFrom(new GridLayout(3, false)).margins(10, 7).create());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
					
		Label labelNamespace = new Label(composite, SWT.NONE);
		labelNamespace.setText("Namespace: ");
		
		GridData inputLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		inputLayoutData.widthHint = 270;
		
		namespaceText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		namespaceText.setLayoutData(GridDataFactory.createFrom(inputLayoutData).span(2, 1).create());
		
		Label labelSchemaLocation = new Label(composite, SWT.NONE);
		labelSchemaLocation.setText("Schema Location: ");
		
		schemaLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		schemaLocationText.setLayoutData(inputLayoutData);
		
		Button browseControl = createBrowseButton(composite);
		browseControl.addSelectionListener(new DropDownSelectionListener(schemaLocationText));
		
		Label labelSchemaType = new Label(composite, SWT.NONE);
		labelSchemaType.setText("Schema Syntax: ");
		
		schemaTypeCombo = new Combo(composite, SWT.READ_ONLY);
		schemaTypeCombo.setLayoutData(GridDataFactory.createFrom(inputLayoutData).span(2, 1).create());
		schemaTypeCombo.add("XML", SCHEMA_TYPE_XML_COMBO_IDX);
		schemaTypeCombo.add("Compact", SCHEMA_TYPE_COMPACT_COMBO_IDX);
		
		errorReportArea = new Label(composite, SWT.NONE);
		errorReportArea.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(3, 1).create());
		
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).span(3, 1).create());		
		
		okButton = createButton(buttonComposite, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(buttonComposite, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		
		if (binding != null) {
			initEditedValues();
		}
		
		revalidate();
		
		ModifyListener validationListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				revalidate();				
			}
		};
		
		namespaceText.addModifyListener(validationListener);
		schemaLocationText.addModifyListener(validationListener);
		schemaTypeCombo.addModifyListener(validationListener);
		
		return composite;
	}
	
	private void initEditedValues() {
		namespaceText.setText(binding.getNamespace());
		schemaLocationText.setText(binding.getSchemaUri().toString());
		
		int selection = binding.getSchemaSyntax() == RngSchemaSyntax.XML ? SCHEMA_TYPE_XML_COMBO_IDX : SCHEMA_TYPE_COMPACT_COMBO_IDX;
		schemaTypeCombo.select(selection);
	}
	
	@Override
	protected void okPressed() {
		String namespace = namespaceText.getText().trim();
		URI schemaUri = UriUtil.resolveUri(getUnresolvedLocation());
		RngSchemaSyntax schemaSyntax = schemaTypeCombo.getSelectionIndex() == SCHEMA_TYPE_XML_COMBO_IDX ? RngSchemaSyntax.XML : RngSchemaSyntax.COMPACT;
		
		RngSchemaBinding newBinding = new RngSchemaBinding(namespace, schemaUri, schemaSyntax);
		
		switch (editMode) {
		case INSERT:
			userBindings.addBinding(newBinding);
			break;
		case UPDATE:
			userBindings.replaceBinding(binding, newBinding);
			break;
		}
		super.okPressed();
	}
	
	@Override
	public boolean close() {
		if (errorReportColor != null) {
			errorReportColor.dispose();
		}
		if (warningReportColor != null) {
			warningReportColor.dispose();
		}
		if (browseImage != null) {
			browseImage.dispose();
		}
		return super.close();
	}
	
	private void revalidate() {
		boolean formValid = true;
		ValidationReport report = new ValidationReport();
		
		formValid = formValid && isNamepaceValid(report);
		formValid = formValid && isLocationValid(report);
		formValid = formValid && isSchemaTypeValid(report);
		
		report.setEntireFormValid(formValid);
		processValidationReport(report);
	}
	
	private boolean isNamepaceValid(ValidationReport report) {
		boolean valid = true;
		String text = namespaceText.getText();
		if (text == null || "".equals(text)) {
			valid = false;
		}
		text = text.trim();
		if (text.contains(" ") || text.contains("\t")) {
			valid = false;
			report.addError("Namespace cannot contain whitespaces.");
		}
		return valid;
	}
	
	private boolean isLocationValid(ValidationReport report) {
		boolean valid = true;
		String text = schemaLocationText.getText();
		if (text == null || "".equals(text)) {
			return false;			
		}
		
		String unresolvedUri = getUnresolvedLocation();
		if (!UriUtil.resourceExists(unresolvedUri)) {
			valid = false;
			report.addError("Schema does not exist at the specified location.");
		}		
		
		return valid;
	}
	
	private String getUnresolvedLocation() {
		String unresolvedLocation = schemaLocationText.getText();
		if (unresolvedLocation == null || "".equals(unresolvedLocation)) {
			return null;
		}
		try {
			if (new URI(unresolvedLocation).getScheme() == null) {
				return UriUtil.convertRelativePathToResourceUri(unresolvedLocation);
			}
		} catch (URISyntaxException e) {}
		
		return unresolvedLocation;
	}
	
	private boolean isSchemaTypeValid(ValidationReport report) {
		String unresolvedLocation = getUnresolvedLocation();
		if (unresolvedLocation != null) {
			if ((unresolvedLocation.endsWith(".rnc") && schemaTypeCombo.getSelectionIndex() == SCHEMA_TYPE_XML_COMBO_IDX) ||
					(unresolvedLocation.endsWith(".xml") && schemaTypeCombo.getSelectionIndex() == SCHEMA_TYPE_COMPACT_COMBO_IDX)) {
				
				report.addWarning("Selected schema syntax does not correspond to the schema file suffix.");
			}
		}
		
		if (schemaTypeCombo.getSelectionIndex() == -1) {
			return false;
		} else {
			return true;
		}
	}
	
	private void processValidationReport(ValidationReport report) {
		okButton.setEnabled(report.isEntireFormValid());
		
		if (report.getErrors().size() > 0) {
			errorReportArea.setForeground(errorReportColor);
			errorReportArea.setText(report.getErrors().get(0));
		} else if (report.getWarnings().size() > 0) {
			errorReportArea.setForeground(warningReportColor);
			errorReportArea.setText(report.getWarnings().get(0));
		} else {
			errorReportArea.setText("");
		}
	}
	
	private static class ValidationReport {
		private boolean valid = false;
		
		private List<String> errors = new LinkedList<String>();
		
		private List<String> warnings = new LinkedList<String>();
		
		void addError(String error) {
			errors.add(error);
		}
		
		void addWarning(String warning) {
			warnings.add(warning);
		}
		
		List<String> getErrors() {
			return errors;
		}
		
		List<String> getWarnings() {
			return warnings;
		}
		
		void setEntireFormValid(boolean valid) {
			this.valid = valid;
		}
		
		boolean isEntireFormValid() {
			return valid;
		}
	}
	
	private static class SchemaFileWorkspaceSelectionDialog extends SelectSingleFileDialog {
		private Combo filterCombo;
		
		public SchemaFileWorkspaceSelectionDialog(Shell parentShell) {
			super(parentShell, null, true);
		}
	
		public void show() {
			create();
			setBlockOnOpen(true);
			getShell().setText("RELAX NG Schema Selection");
			setTitle("Select RELAX NG Schema");
			setMessage("Select RELAX NG Schema that should be bound to the given namespace.");
			addFilterExtensions(new String[] {".xml", ".rnc", ".rng"});
			open();
		}
	}
	
	private class DropDownSelectionListener extends SelectionAdapter {
		private Menu menu;
		private Control control;

		DropDownSelectionListener(Control aControl) {
			super();
			this.control = aControl;
		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			// Create the menu if it has not already been created
			if (menu == null) {
				// Lazy create the menu.
				menu = new Menu(getShell());
				MenuItem menuItem = new MenuItem(menu, SWT.NONE);
				menuItem.setText("Workspace...");
				/*
				 * Add a menu selection listener so that the menu is hidden
				 * when the user selects an item from the drop down menu.
				 */
				menuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setMenuVisible(false);
						invokeWorkspaceFileSelectionDialog();
					}
				});

				menuItem = new MenuItem(menu, SWT.NONE);
				menuItem.setText("File System...");
				/*
				 * Add a menu selection listener so that the menu is hidden
				 * when the user selects an item from the drop down menu.
				 */
				menuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setMenuVisible(false);
						invokeFileSelectionDialog();
					}
				});

			}

			// Position the menu below and vertically aligned with the the
			// drop down tool button.
			Button button = (Button) event.widget;

			// set location
			Point ptBrowse = button.getLocation();
			Rectangle rcBrowse = button.getBounds();
			ptBrowse.y += rcBrowse.height;
			ptBrowse = button.getParent().toDisplay(ptBrowse);
			menu.setLocation(ptBrowse.x, ptBrowse.y);

			setMenuVisible(true);

		}

		void setMenuVisible(boolean visible) {
			menu.setVisible(visible);
			// this.visible = visible;
		}

		void invokeWorkspaceFileSelectionDialog() {
			SchemaFileWorkspaceSelectionDialog dialog = new SchemaFileWorkspaceSelectionDialog(getShell());
			dialog.show();

			IFile file = dialog.getFile();
			if (dialog.getReturnCode() == Window.OK && file != null) {
				// remove leading slash from the value to avoid the
				// whole leading slash ambiguity problem
				String uri = file.getFullPath().toString();
				while (uri.startsWith("/") || uri.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
					uri = uri.substring(1);
				}
				
				deriveSchemaSyntax(uri);
				
				if (control instanceof Text) {
					((Text) control).setText(uri);
				}
			}
		}

		void invokeFileSelectionDialog() {
			FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
			String file = dialog.open();
			
			deriveSchemaSyntax(file);
			
			if ((control instanceof Text) && (file != null)) {
				((Text) control).setText("file:/" + file);
			} 
		}
		
		private void deriveSchemaSyntax(String path) {
			if (path.endsWith(".rnc")) {
				schemaTypeCombo.select(SCHEMA_TYPE_COMPACT_COMBO_IDX);
			} else if (path.endsWith(".xml")) {
				schemaTypeCombo.select(SCHEMA_TYPE_XML_COMBO_IDX);
			}
		}
	}
	
	private  Button createBrowseButton(Composite composite) {
		Button browseButton = new Button(composite, SWT.FLAT);
		browseButton.setImage(browseImage);
		Rectangle r = browseImage.getBounds();
		GridData gd = new GridData();
		int IMAGE_WIDTH_MARGIN = 6;
		int IMAGE_HEIGHT_MARGIN = 6;
		gd.heightHint = r.height + IMAGE_HEIGHT_MARGIN;
		gd.widthHint = r.width + IMAGE_WIDTH_MARGIN;
		browseButton.setLayoutData(gd);
		return browseButton;
	}
	
	private enum EditMode {
		INSERT, UPDATE;
	}
}