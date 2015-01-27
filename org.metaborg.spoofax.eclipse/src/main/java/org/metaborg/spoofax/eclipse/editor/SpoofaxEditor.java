package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.processing.Processor;

public class SpoofaxEditor extends TextEditor {
    final Processor processor;

    private IDocumentListener documentListener;


    public SpoofaxEditor() {
        super();

        this.processor = SpoofaxPlugin.injector().getInstance(Processor.class);

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration(this, getSourceViewer()));
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler,
        int styles) {
        final ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);


        final IEditorInput input = getEditorInput();
        final IDocument document = getDocumentProvider().getDocument(input);
        documentListener = new IDocumentListener() {
            @Override public void documentChanged(DocumentEvent event) {
                processor.editorUpdate(input, sourceViewer, event.getDocument().get());
            }

            @Override public void documentAboutToBeChanged(DocumentEvent event) {

            }
        };
        document.addDocumentListener(documentListener);
        processor.editorOpen(input, sourceViewer, document.get());

        return sourceViewer;
    }

    @Override public void dispose() {
        final IEditorInput input = getEditorInput();
        final IDocument document = getDocumentProvider().getDocument(input);
        document.removeDocumentListener(documentListener);
        processor.editorClose(input);
        super.dispose();
    }
}
