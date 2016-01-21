package org.metaborg.spoofax.core.project;

import com.google.inject.Inject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.settings.ILegacySpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;

/**
 * This class is used to temporarily bridge between the old and new configuration systems.
 */
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecPathsService implements ISpoofaxLanguageSpecPathsService {

    private final ILegacySpoofaxProjectSettingsService settingsService;

    @Inject
    public LegacySpoofaxLanguageSpecPathsService(final ILegacySpoofaxProjectSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public ISpoofaxLanguageSpecPaths get(ILanguageSpec languageSpec) {
        return get((IProject)languageSpec);
    }

    public ISpoofaxLanguageSpecPaths get(IProject project) {
        try {
            final LegacySpoofaxProjectSettings settings = this.settingsService.get(project);
            return new LegacySpoofaxLanguageSpecPaths(settings);
        } catch (ProjectException e) {
            throw new RuntimeException(e);
        }
    }
}
