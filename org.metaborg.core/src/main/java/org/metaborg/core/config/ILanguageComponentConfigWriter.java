package org.metaborg.core.config;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;
import mb.util.vfs2.file.IFileAccess;

/**
 * Writes a configuration for the specified {@link ILanguageComponent}.
 */
public interface ILanguageComponentConfigWriter {
    /**
     * Writes the specified configuration for the specified language component.
     *
     * @param root
     *            The language root folder.
     * @param config
     *            The configuration to write.
     * @param access
     */
    void write(FileObject root, ILanguageComponentConfig config, @Nullable IFileAccess access) throws ConfigException;
}
