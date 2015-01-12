package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

public class SpoofaxEditor extends TextEditor {
    public SpoofaxEditor() {
        super();

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer =
            new SpoofaxSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        return viewer;
    }
}
