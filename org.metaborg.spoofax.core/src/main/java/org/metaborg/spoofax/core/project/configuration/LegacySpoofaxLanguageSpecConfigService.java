package org.metaborg.spoofax.core.project.configuration;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.core.project.configuration.*;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.ISpoofaxProjectSettings;
import org.metaborg.spoofax.core.project.settings.ISpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This class is only used for the configuration system migration.
 */
public class LegacySpoofaxLanguageSpecConfigService implements ISpoofaxLanguageSpecConfigService {

    private final ConfigurationBasedSpoofaxLanguageSpecConfigService configurationBasedLanguageSpecConfigService;
    private final ISpoofaxProjectSettingsService settingsService;
    private final ISpoofaxLanguageSpecConfigWriter configWriter;

    @Inject
    public LegacySpoofaxLanguageSpecConfigService(final ConfigurationBasedSpoofaxLanguageSpecConfigService configurationBasedLanguageSpecConfigService,
                                                  final ISpoofaxProjectSettingsService settingsService, final ISpoofaxLanguageSpecConfigWriter configWriter) {
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
        if (config == null) {
            final SpoofaxProjectSettings settings;
            try {
                settings = this.settingsService.get(languageSpec);/*new IProject() {
                    @Override
                    public FileObject location() {
                        return languageSpec.location();
                    }
                });*/
            } catch (ProjectException e) {
                throw new RuntimeException(e);
            }
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacySpoofaxLanguageSpecConfig(settings);

                // Write the configuration to file.
                this.configWriter.write(languageSpec, config);
            }
        }
        return config;
    }
}
