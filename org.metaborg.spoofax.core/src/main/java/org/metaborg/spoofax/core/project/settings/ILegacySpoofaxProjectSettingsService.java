package org.metaborg.spoofax.core.project.settings;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;

/**
 * Interface for getting Spoofax-specific project settings.
 *
 * @deprecated Use {@link ISpoofaxLanguageSpecConfigService} instead.
 */
@Deprecated
public interface ILegacySpoofaxProjectSettingsService {
    /**
     * Checks if Spoofax-specific project settings are available given a location.
     * 
     * @param location
     *            Location to get settings for.
     * @return True if available, false otherwise.
     */
    boolean available(FileObject location);

    /**
     * Checks if Spoofax-specific project settings are available given a project.
     * 
     * @param project
     *            Project to get settings for.
     * @return True if available, false otherwise.
     */
    boolean available(IProject project);

    @Nullable LegacySpoofaxProjectSettings get(FileObject location) throws ProjectException;

    /**
     * Retrieves Spoofax-specific project settings given a project.
     * 
     * @param project
     *            Project to get settings for.
     * @return Spoofax-specific project settings, or null if no settings could be retrieved.
     * @throws ProjectException
     *             When creating project settings unexpectedly fails.
     */
    @Nullable LegacySpoofaxProjectSettings get(IProject project) throws ProjectException;
}
