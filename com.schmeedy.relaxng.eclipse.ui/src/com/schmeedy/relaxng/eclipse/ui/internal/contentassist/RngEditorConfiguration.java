package com.schmeedy.relaxng.eclipse.ui.internal.contentassist;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.xml.core.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;


public class RngEditorConfiguration extends	StructuredTextViewerConfigurationXML {

	public RngEditorConfiguration() {}
	
	@Override
	protected IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {
		
		if (partitionType == IStructuredPartitions.DEFAULT_PARTITION
				|| partitionType == IXMLPartitions.XML_DEFAULT) {
			return new IContentAssistProcessor[]  {new RngContentAssistProcessor()};
		} else {
			return super.getContentAssistProcessors(sourceViewer, partitionType);
		}
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IContentAssistant contentAssistant = super.getContentAssistant(sourceViewer);
		if (contentAssistant instanceof ContentAssistant) {
			((ContentAssistant) contentAssistant).setAutoActivationDelay(0);
		}
		return contentAssistant;
	}
}
