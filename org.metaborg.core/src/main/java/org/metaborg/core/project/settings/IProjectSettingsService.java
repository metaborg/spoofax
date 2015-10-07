package org.metaborg.core.project.settings;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

/**
 * Interface for getting project settings.
 */
public interface IProjectSettingsService {
    /**
     * Retrieves project settings for given project.
     * 
     * @param project
     *            Project to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     */
    public abstract @Nullable IProjectSettings get(IProject project);

    /**
     * Retrieves project settings at given location. Use when a Metaborg project is not available.
     * 
     * @param location
     *            Location to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     */
    public abstract @Nullable IProjectSettings get(FileObject location);
}
