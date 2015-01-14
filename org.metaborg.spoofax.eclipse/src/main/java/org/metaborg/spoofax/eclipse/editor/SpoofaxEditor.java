package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.ui.editors.text.TextEditor;

public class SpoofaxEditor extends TextEditor {
    public SpoofaxEditor() {
        super();

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration(this));
    }
}
