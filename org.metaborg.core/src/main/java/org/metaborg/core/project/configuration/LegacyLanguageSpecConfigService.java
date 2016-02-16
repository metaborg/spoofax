package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfigService implements ILanguageSpecConfigService {
    private final ConfigurationBasedLanguageSpecConfigService languageSpecConfigService;
    private final ILegacyProjectSettingsService settingsService;


    @Inject public LegacyLanguageSpecConfigService(
        ConfigurationBasedLanguageSpecConfigService languageSpecConfigService,
        ILegacyProjectSettingsService settingsService) {
        this.languageSpecConfigService = languageSpecConfigService;
        this.settingsService = settingsService;
    }

    @Override public @Nullable ILanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException {
        ILanguageSpecConfig config = this.languageSpecConfigService.get(languageSpec);
        if(config == null) {
            final ILegacyProjectSettings settings = this.settingsService.get(languageSpec);
            if(settings != null) {
                config = new LegacyLanguageSpecConfig(settings);
            }
        }
        return config;
    }

    @Override public @Nullable ILanguageSpecConfig get(FileObject rootFolder) throws IOException {
        ILanguageSpecConfig config = this.languageSpecConfigService.get(rootFolder);
        if(config == null) {
            final ILegacyProjectSettings settings = this.settingsService.get(rootFolder);
            if(settings != null) {
                config = new LegacyLanguageSpecConfig(settings);
            }
        }
        return config;
    }
}
