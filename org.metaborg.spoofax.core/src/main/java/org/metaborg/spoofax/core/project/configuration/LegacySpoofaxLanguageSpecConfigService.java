package org.metaborg.spoofax.core.project.configuration;

import com.google.inject.Inject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.settings.ILegacySpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfigService implements ISpoofaxLanguageSpecConfigService {

    private final ConfigurationBasedSpoofaxLanguageSpecConfigService configurationBasedLanguageSpecConfigService;
    private final ILegacySpoofaxProjectSettingsService settingsService;
    private final ISpoofaxLanguageSpecConfigWriter configWriter;

    @Inject
    public LegacySpoofaxLanguageSpecConfigService(final ConfigurationBasedSpoofaxLanguageSpecConfigService configurationBasedLanguageSpecConfigService,
                                                  final ILegacySpoofaxProjectSettingsService settingsService, final ISpoofaxLanguageSpecConfigWriter configWriter) {
        this.configurationBasedLanguageSpecConfigService = configurationBasedLanguageSpecConfigService;
        this.settingsService = settingsService;
        this.configWriter = configWriter;
    }

    @Nullable
    @Override
    public ISpoofaxLanguageSpecConfig get(final ILanguageSpec languageSpec) throws IOException {
        // Try get a configuration.
        @Nullable ISpoofaxLanguageSpecConfig config = this.configurationBasedLanguageSpecConfigService.get(languageSpec);

        // If this fails, try get project settings.
//        // FIXME: Only do this when the new `config == null`!
        if (config == null && languageSpec instanceof IProject) {
//        if (languageSpec instanceof IProject) {
            @Nullable final LegacySpoofaxProjectSettings settings;
            try {
                settings = this.settingsService.get((IProject) languageSpec);
            } catch (ProjectException e) {
                throw new IOException(e);
            }
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacySpoofaxLanguageSpecConfig(settings);

//                // Write the configuration to file.
//                // FIXME: This is only for migrating the old settings system to the new.
//                this.configWriter.write(languageSpec, config, null);
            }
        }

        return config;
    }
}
