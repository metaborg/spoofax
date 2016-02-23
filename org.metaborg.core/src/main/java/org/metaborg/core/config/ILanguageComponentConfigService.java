package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Stores and retrieves language component configurations.
 */
public interface ILanguageComponentConfigService {
    /**
     * Checks if a configuration exists for the language component at given location.
     *
     * @param rootFolder
     *            The language component root folder.
     * @return True if a configuration exists, false otherwise.
     */
    boolean available(FileObject rootFolder);

    /**
     * Gets the configuration for the language component at given location.
     *
     * @param rootFolder
     *            The language component root folder.
     * @return The configuration, or <code>null</code> when no configuration could be retrieved.
     * @throws ConfigException
     *             When reading the configuration fails.
     */
    @Nullable ILanguageComponentConfig get(FileObject rootFolder) throws ConfigException;
}
