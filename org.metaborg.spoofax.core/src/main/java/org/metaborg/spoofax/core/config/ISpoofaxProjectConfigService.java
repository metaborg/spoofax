package org.metaborg.spoofax.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.project.IProject;

/**
 * Stores and retrieves language component configurations.
 */
public interface ISpoofaxProjectConfigService extends IProjectConfigService {
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
    ConfigRequest<ISpoofaxProjectConfig> get(FileObject rootFolder);

    /**
     * Gets the configuration for the given project.
     * 
     * @param project
     *            The project.
     * @return The configuration, or null.
     */
    @Nullable ISpoofaxProjectConfig get(IProject project);
}
