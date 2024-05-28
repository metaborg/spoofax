package org.metaborg.spoofax.core.config;

import jakarta.annotation.Nullable;

import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import mb.util.vfs2.file.IFileAccess;

/**
 * Writes a configuration for an {@link IProject}.
 */
public interface ISpoofaxProjectConfigWriter {
    /**
     * Writes the specified configuration for the specified project.
     *
     * @param project
     *            The project.
     * @param config
     *            The configuration to write.
     * @param access
     */
    void write(IProject project, ISpoofaxProjectConfig config, @Nullable IFileAccess access) throws ConfigException;
}
