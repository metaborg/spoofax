package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
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
}
