package org.strategoxt.imp.runtime.editor;

import org.eclipse.imp.editor.UniversalEditor.StructuredSourceViewerConfiguration;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * @author Oskar van Rest
 */
public class SpoofaxSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private StructuredSourceViewerConfiguration structuredSourceViewerConfiguration;
	
	public SpoofaxSourceViewerConfiguration(StructuredSourceViewerConfiguration structuredSourceViewerConfiguration) {
		this.structuredSourceViewerConfiguration = structuredSourceViewerConfiguration;
	}
	
	public StructuredSourceViewerConfiguration getStructuredSourceViewerConfiguration() {
		return structuredSourceViewerConfiguration;
	}
	
	/**
	 * IMP delegation below. To be incrementally replaced.
	 */
	
	@Override
    public int getTabWidth(ISourceViewer sourceViewer) {
    	return structuredSourceViewerConfiguration.getTabWidth(sourceViewer);
    }
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getPresentationReconciler(sourceViewer);
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getContentAssistant(sourceViewer);
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getAnnotationHover(sourceViewer);
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return structuredSourceViewerConfiguration.getAutoEditStrategies(sourceViewer, contentType);
	}

	@Override
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getContentFormatter(sourceViewer);
	}

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getHyperlinkDetectors(sourceViewer);
    }

	@Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getInformationControlCreator(sourceViewer);
    }

	@Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return structuredSourceViewerConfiguration.getInformationPresenter(sourceViewer);
    }

	@Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return structuredSourceViewerConfiguration.getTextHover(sourceViewer, contentType);
    }
}
