package org.metaborg.spoofax.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ProjectException;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.ILegacySpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfigService implements ISpoofaxLanguageSpecConfigService {
    private final ConfigurationBasedSpoofaxLanguageSpecConfigService languageSpecConfigService;
    private final ILegacySpoofaxProjectSettingsService settingsService;


    @Inject public LegacySpoofaxLanguageSpecConfigService(
        ConfigurationBasedSpoofaxLanguageSpecConfigService languageSpecConfigService,
        ILegacySpoofaxProjectSettingsService settingsService) {
        this.languageSpecConfigService = languageSpecConfigService;
        this.settingsService = settingsService;
    }


    @Override public @Nullable ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException {
        final ISpoofaxLanguageSpecConfig config = languageSpecConfigService.get(languageSpec);
        if(config == null) {
            final LegacySpoofaxProjectSettings settings;
            try {
                settings = this.settingsService.get(languageSpec);
            } catch(ProjectException e) {
                throw new IOException(e);
            }

            if(settings != null) {
                return new LegacySpoofaxLanguageSpecConfig(settings);
            }
        }
        return config;
    }

    @Override public @Nullable ILanguageSpecConfig get(FileObject rootFolder) throws IOException {
        return languageSpecConfigService.get(rootFolder);
    }
}
