package org.metaborg.core.project.settings;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.configuration.ILanguageComponentConfigService;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;

/**
 * Interface for getting project settings.
 *
 * @deprecated Use {@link ILanguageComponentConfigService} instead.
 */
@Deprecated
public interface ILegacyProjectSettingsService {
    /**
     * Retrieves project settings for given project.
     * 
     * @param project
     *            Project to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     *
     * @deprecated Use {@link ILanguageSpecConfigService#get(ILanguageSpec)}
     */
    @Deprecated @Nullable ILegacyProjectSettings get(IProject project);

    /**
     * Retrieves project settings at given location. Use when a Metaborg project is not available.
     * 
     * @param location
     *            Location to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     * @deprecated Use {@link ILanguageComponentConfigService#get(ILanguageComponent)}
     */
    @Deprecated @Nullable ILegacyProjectSettings get(FileObject location);
}
