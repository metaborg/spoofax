package org.metaborg.core.editor;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;

/**
 * Interface for an editor in an IDE.
 */
public interface IEditor {
    /**
     * @return Current resource, or null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable FileObject resource();

    /**
     * @return Language of the current input/document, or null if the editor has not been initialized yet, if it has
     *         been disposed, or if the editor was opened before languages were loaded.
     */
    public abstract @Nullable ILanguage language();

    /**
     * @return If this editor is enabled.
     */
    public abstract boolean enabled();

    /**
     * Enables parsing, analysis, and editor services. Does nothing if editor has not been initialized, or if it has
     * been disposed, or if the editor is already enabled.
     */
    public abstract void enable();

    /**
     * Disables parsing, analysis, and editor services. Does nothing if editor has not been initialized, or if it has
     * been disposed, or if the editor is already disabled.
     */
    public abstract void disable();

    /**
     * Force a parser, analysis, and editor services update. Does nothing if editor has not been initialized, or if it
     * has been disposed.
     */
    public abstract void forceUpdate();

    /**
     * Reconfigure the editor, causing its language to be updated and its source viewer to be reconfigured. Source
     * viewer reconfiguration will be executed on the UI thread. Does nothing if editor has not been initialized, or if
     * it has been disposed.
     */
    public abstract void reconfigure();
}
