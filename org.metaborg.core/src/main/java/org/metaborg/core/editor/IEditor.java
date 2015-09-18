package org.metaborg.core.editor;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for an editor in an IDE.
 */
public interface IEditor {
    /**
     * @return Current resource. Null if the editor has not been initialized yet, or if it has been disposed.
     */
    public abstract @Nullable FileObject resource();

    /**
     * @return Language of the current input resource. Null if the editor has not been initialized yet, if it has been
     *         disposed, or if the editor was opened before languages were loaded.
     */
    public abstract @Nullable ILanguageImpl language();

    /**
     * @return If this editor is enabled.
     */
    public abstract boolean enabled();

    /**
     * Enables parsing, analysis, and implementation-specific services. Does nothing if editor has not been initialized,
     * or if it has been disposed, or if the editor is already enabled.
     */
    public abstract void enable();

    /**
     * Disables parsing, analysis, and implementation-specific services. Does nothing if editor has not been
     * initialized, or if it has been disposed, or if the editor is already disabled.
     */
    public abstract void disable();

    /**
     * Force a parser, analysis, and implementation-specific update. Does nothing if editor has not been initialized, or
     * if it has been disposed.
     */
    public abstract void forceUpdate();

    /**
     * Reconfigure the editor, updates its language and performs any implementation-specific reconfiguration. Does
     * nothing if editor has not been initialized, or if it has been disposed.
     */
    public abstract void reconfigure();
}
