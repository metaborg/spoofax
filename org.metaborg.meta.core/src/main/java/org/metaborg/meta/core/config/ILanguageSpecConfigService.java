package org.metaborg.meta.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Stores and retrieves language specification configurations.
 */
public interface ILanguageSpecConfigService {
    /**
     * Checks if a configuration exists for the language specification at the given location.
     *
     * @param rootFolder
     *            The language specification root folder.
     * @return True if a configuration exists, false otherwise.
     * @throws IOException
     *             When location the configuration fails.
     */
    boolean available(FileObject rootFolder) throws IOException;

    /**
     * Gets the configuration for the language specification at the given location.
     *
     * @param rootFolder
     *            The language specification root folder.
     * @return The configuration, or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     *             When reading the configuration fails.
     */
    @Nullable ILanguageSpecConfig get(FileObject rootFolder) throws IOException;
}
