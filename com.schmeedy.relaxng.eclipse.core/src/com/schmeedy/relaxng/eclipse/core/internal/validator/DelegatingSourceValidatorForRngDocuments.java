package com.schmeedy.relaxng.eclipse.core.internal.validator;


import org.eclipse.wst.validation.internal.provisional.core.IValidator;
import org.eclipse.wst.xml.ui.internal.validation.DelegatingSourceValidator;

@SuppressWarnings("restriction")
public class DelegatingSourceValidatorForRngDocuments extends DelegatingSourceValidator {
	private IValidator validationDelegate = new JingValidationDelegate();
	
	public DelegatingSourceValidatorForRngDocuments() {}

	@Override
	protected IValidator getDelegateValidator() {
		return validationDelegate;
	}
}
