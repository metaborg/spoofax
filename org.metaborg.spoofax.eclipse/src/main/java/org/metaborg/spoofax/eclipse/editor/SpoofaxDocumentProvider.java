package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class SpoofaxDocumentProvider extends FileDocumentProvider {
    @Override protected IDocument createDocument(Object element) throws CoreException {
        return super.createDocument(element);
    }

    @Override protected IDocument createEmptyDocument() {
        return super.createEmptyDocument();
    }
}
