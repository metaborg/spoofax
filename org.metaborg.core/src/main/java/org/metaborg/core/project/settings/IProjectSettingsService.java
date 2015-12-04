package org.metaborg.core.project.settings;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;

/**
 * Interface for getting project settings.
 *
 * @deprecated Use {@link ILanguageComponentConfigService} instead.
 */
@Deprecated
public interface IProjectSettingsService {

    /**
     * Retrieves project settings for given project.
     * 
     * @param project
     *            Project to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     *
     * @deprecated Use {@link ILanguageSpecConfigService#get(ILanguageSpec)}
     */
    @Deprecated
    public abstract @Nullable IProjectSettings get(IProject project);

    /**
     * Retrieves project settings at given location. Use when a Metaborg project is not available.
     * 
     * @param location
     *            Location to get settings for.
     * @return Project settings, or null when no settings could be retrieved.
     * @deprecated Use {@link ILanguageComponentConfigService#get(ILanguageImpl)}
     */
    @Deprecated
    public abstract @Nullable IProjectSettings get(FileObject location);
}
