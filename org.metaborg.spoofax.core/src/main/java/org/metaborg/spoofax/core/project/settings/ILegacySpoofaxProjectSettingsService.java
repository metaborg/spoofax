package org.metaborg.spoofax.core.project.settings;

import javax.annotation.Nullable;

import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfigService;

/**
 * Interface for getting Spoofax-specific project settings.
 *
 * @deprecated Use {@link ISpoofaxLanguageSpecConfigService} instead.
 */
@Deprecated
public interface ILegacySpoofaxProjectSettingsService {
    /**
     * Retrieves Spoofax-specific project settings given a project.
     * 
     * @param project
     *            Project to get settings for.
     * @return Spoofax-specific project settings, or null if no settings could be retrieved.
     * @throws ProjectException
     *             When creating project settings unexpectedly fails.
     */
    public abstract @Nullable
    LegacySpoofaxProjectSettings get(IProject project) throws ProjectException;
}
