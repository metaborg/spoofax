package org.metaborg.spoofax.core.project;

import com.google.inject.Inject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.Project;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.settings.ISpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

/**
 * This class is used to temporarily bridge between the old and new configuration systems.
 */
public class LegacySpoofaxLanguageSpecPathsService implements ISpoofaxLanguageSpecPathsService {

    private final ISpoofaxProjectSettingsService settingsService;

    @Inject
    public LegacySpoofaxLanguageSpecPathsService(final ISpoofaxProjectSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public ISpoofaxLanguageSpecPaths get(ILanguageSpec languageSpec) {
        return get((IProject)languageSpec);
    }

    public ISpoofaxLanguageSpecPaths get(IProject project) {
        try {
            final SpoofaxProjectSettings settings = this.settingsService.get(project);
            return new LegacySpoofaxLanguageSpecPaths(settings);
        } catch (ProjectException e) {
            throw new RuntimeException(e);
        }
    }
}
