package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SpoofaxSourceViewerConfiguration extends SourceViewerConfiguration {
    private final SpoofaxEditor editor;


    public SpoofaxSourceViewerConfiguration(SpoofaxEditor editor) {
        super();

        this.editor = editor;
    }


    @Override public IReconciler getReconciler(ISourceViewer sourceViewer) {
        return new MonoReconciler(new SpoofaxReconcilingStrategy(editor), false);
    }

    @Override public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }
    
    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new DefaultTextHover(sourceViewer);
    }
}
