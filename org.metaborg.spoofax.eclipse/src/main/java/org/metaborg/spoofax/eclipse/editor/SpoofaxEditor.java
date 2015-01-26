package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.processing.Processor;

public class SpoofaxEditor extends TextEditor {
    public SpoofaxEditor() {
        super();

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration(this, getSourceViewer()));
    }

    @Override public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler,
        int styles) {
        final ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);

        final Processor processor = SpoofaxPlugin.injector().getInstance(Processor.class);
        processor.editorOpen(getEditorInput(), getDocumentProvider().getDocument(getEditorInput()),
            sourceViewer);

        return sourceViewer;
    }
}
