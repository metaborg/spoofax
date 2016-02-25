package org.metaborg.core.config;

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
     * @return Configuration request, either with a valid configuration, or a collection of error messages.
     */
    ConfigRequest<ILanguageComponentConfig> get(FileObject rootFolder);
}
