package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SpoofaxSourceViewerConfiguration extends SourceViewerConfiguration {
    private final SpoofaxEditor editor;
    private final ISourceViewer sourceViewer;


    public SpoofaxSourceViewerConfiguration(SpoofaxEditor editor, ISourceViewer sourceViewer) {
        super();
        this.editor = editor;
        this.sourceViewer = sourceViewer;
    }


    @Override public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }

    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new DefaultTextHover(sourceViewer);
    }
}
