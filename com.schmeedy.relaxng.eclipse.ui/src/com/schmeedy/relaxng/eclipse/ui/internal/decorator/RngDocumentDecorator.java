package com.schmeedy.relaxng.eclipse.ui.internal.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.schmeedy.relaxng.eclipse.ui.internal.RngUiPlugin;

public class RngDocumentDecorator implements ILightweightLabelDecorator {
	private static final String RELAX_NG_CONTENT_TYPE = "com.schmeedy.relaxng.eclipse.core.relaxngDocument";
	private static final String OVERLAY_ICON = "rng_document_ovr.gif"; //NON-NLS-1

	private ImageDescriptor ovrIconDescriptor;

	public RngDocumentDecorator() {
		ovrIconDescriptor = RngUiPlugin.getDefault().getIcon(OVERLAY_ICON);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (ovrIconDescriptor == null) {
			return;
		}
		IResource resource = (IResource) element;
		if (resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			try {
				if (file.getContentDescription() != null && RELAX_NG_CONTENT_TYPE.equals(file.getContentDescription().getContentType().getId())) {
					decoration.addOverlay(ovrIconDescriptor, IDecoration.TOP_RIGHT);
				}
			} catch (CoreException e) {
				// do nothing
			}				
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}