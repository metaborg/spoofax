package org.metaborg.meta.core.config;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigRequest;

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
     */
    boolean available(FileObject rootFolder);

    /**
     * Gets the configuration for the language specification at the given location.
     *
     * @param rootFolder
     *            The language specification root folder.
     * @return Configuration request, either with a valid configuration, or a collection of error messages.
     */
    ConfigRequest<? extends ILanguageSpecConfig> get(FileObject rootFolder);
}
