package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.metaborg.spoofax.core.editor.IEditor;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.util.Nullable;

public interface IEclipseEditor extends IEditor {
    public static final String id = SpoofaxPlugin.id + ".editor";


    /**
     * @return Current input, or null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable IEditorInput input();

    /**
     * @return Current Eclipse resource, or null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable IResource eclipseResource();

    /**
     * @return Current document, or null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable IDocument document();

    /**
     * @return Source viewer, or null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable ISourceViewer sourceViewer();

    /**
     * @return Source viewer configuration, or null if the editor has not been initialized yet, or if it has been
     *         disposed.
     */
    public abstract @Nullable SourceViewerConfiguration configuration();
}