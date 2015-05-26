package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.metaborg.spoofax.eclipse.util.Nullable;


public interface ISpoofaxEditorListener {
    /**
     * @return All open Spoofax editors.
     */
    public abstract Iterable<ISpoofaxEclipseEditor> openEditors();

    /**
     * @return Current active Spoofax editor, or null if none.
     */
    public abstract @Nullable ISpoofaxEclipseEditor currentEditor();

    /**
     * Returns the previously active Spoofax editor when a Spoofax editor was active, followed by activation of a
     * non-editor part, like the package explorer or outline. When a non-Spoofax editor is activated, such as a JDT
     * editor, this returns null again.
     * 
     * Returns the same value as {@link #currentActive} if a Spoofax editor is currently active.
     * 
     * @return Previously active Spoofax editor, or null if none.
     */
    public abstract @Nullable ISpoofaxEclipseEditor previousEditor();

    public abstract void register();

}