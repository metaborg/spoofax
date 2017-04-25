package org.metaborg.core.config;

import org.apache.commons.vfs2.FileObject;

/**
 * Stores and retrieves language component configurations.
 */
public interface IProjectConfigService {
    /**
     * Checks if a configuration exists for the project at the given location.
     *
     * @param rootFolder
     *            The project root folder.
     * @return True if a configuration exists, false otherwise.
     */
    boolean available(FileObject rootFolder);

    /**
     * Gets the configuration for the project at the given location.
     *
     * @param rootFolder
     *            The project root folder.
     * @return Configuration request, either with a valid configuration, or a collection of error messages.
     */
    ConfigRequest<? extends IProjectConfig> get(FileObject rootFolder);

    /**
     * Gets the default configuration for the project at the given location.
     *
     * @param rootFolder
     *            The project root folder.
     * @return Default project configuration.
     */
    IProjectConfig defaultConfig(FileObject rootFolder);
}