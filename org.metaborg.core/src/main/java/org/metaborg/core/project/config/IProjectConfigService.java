package org.metaborg.core.project.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

/**
 * Stores and retrieves language component configurations.
 */
public interface IProjectConfigService {
    /**
     * Gets the configuration for the given project.
     *
     * @param project
     *            The project to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     */
    @Nullable IProjectConfig get(IProject project) throws IOException;

    /**
     * Gets the configuration for the project at the given location.
     *
     * @param rootFolder
     *            The project root folder.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     */
    @Nullable IProjectConfig get(FileObject rootFolder) throws IOException;
}
