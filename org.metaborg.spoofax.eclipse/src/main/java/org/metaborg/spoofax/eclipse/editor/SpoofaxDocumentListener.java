package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.metaborg.spoofax.eclipse.processing.Processor;

public class SpoofaxDocumentListener implements IDocumentListener {
    private final IEditorInput input;
    private final ISourceViewer sourceViewer;
    private final Processor processor;
    private final PresentationMerger presentationMerger;


    public SpoofaxDocumentListener(IEditorInput input, ISourceViewer sourceViewer, Processor processor,
        PresentationMerger presentationMerger) {
        this.input = input;
        this.sourceViewer = sourceViewer;
        this.processor = processor;
        this.presentationMerger = presentationMerger;
    }


    @Override public void documentAboutToBeChanged(DocumentEvent event) {

    }

    @Override public void documentChanged(DocumentEvent event) {
        processor.editorChange(input, sourceViewer, event.getDocument().get(), presentationMerger);
    }
}
