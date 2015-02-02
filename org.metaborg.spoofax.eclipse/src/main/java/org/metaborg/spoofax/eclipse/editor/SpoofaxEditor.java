package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.processing.Processor;

public class SpoofaxEditor extends TextEditor {
    private final Processor processor;
    private final IPropertyListener editorInputListener;

    private IEditorInput currentInput;
    private IDocument currentDocument;
    private IDocumentListener currentDocumentListener;


    public SpoofaxEditor() {
        super();

        this.processor = SpoofaxPlugin.injector().getInstance(Processor.class);
        this.editorInputListener = new IPropertyListener() {
            @Override public void propertyChanged(Object source, int propId) {
                if(propId == IEditorPart.PROP_INPUT) {
                    editorInputChanged();
                }
            }
        };

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler,
        int styles) {
        final ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);

        // Store current input and document so we have access to them when the editor input changes.
        currentInput = getEditorInput();
        currentDocument = getDocumentProvider().getDocument(currentInput);
        currentDocumentListener =
            new SpoofaxDocumentListener(currentInput, sourceViewer, processor);
        currentDocument.addDocumentListener(currentDocumentListener);

        // Register for changes in the editor input, to handle renaming or moving of resources of
        // open editors.
        this.addPropertyListener(editorInputListener);

        processor.editorOpen(currentInput, sourceViewer, currentDocument.get());

        return sourceViewer;
    }

    @Override public void dispose() {
        currentDocument.removeDocumentListener(currentDocumentListener);
        this.removePropertyListener(editorInputListener);
        processor.editorClose(currentInput);
        super.dispose();
    }


    private void editorInputChanged() {
        final IEditorInput oldInput = currentInput;
        final IDocument oldDocument = currentDocument;
        final ISourceViewer sourceViewer = getSourceViewer();

        // Unregister old document listener and register a new one, because the input changed which
        // also changes the document.
        oldDocument.removeDocumentListener(currentDocumentListener);
        currentInput = getEditorInput();
        currentDocument = getDocumentProvider().getDocument(currentInput);
        currentDocumentListener =
            new SpoofaxDocumentListener(currentInput, sourceViewer, processor);
        currentDocument.addDocumentListener(currentDocumentListener);

        processor.editorInputChange(oldInput, currentInput, sourceViewer, currentDocument.get());
    }
}
