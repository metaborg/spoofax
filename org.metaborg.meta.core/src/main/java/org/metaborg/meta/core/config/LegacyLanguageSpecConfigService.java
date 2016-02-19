package org.metaborg.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfigService implements ILanguageSpecConfigService {
    private final LanguageSpecConfigService languageSpecConfigService;
    private final ILegacyProjectSettingsService settingsService;


    @Inject public LegacyLanguageSpecConfigService(LanguageSpecConfigService languageSpecConfigService,
        ILegacyProjectSettingsService settingsService) {
        this.languageSpecConfigService = languageSpecConfigService;
        this.settingsService = settingsService;
    }

    @Override public boolean available(FileObject rootFolder) {
        return languageSpecConfigService.available(rootFolder) || settingsService.get(rootFolder) != null;
    }

    @Override public @Nullable ILanguageSpecConfig get(FileObject rootFolder) throws ConfigException {
        final ILanguageSpecConfig config = this.languageSpecConfigService.get(rootFolder);
        if(config == null) {
            final ILegacyProjectSettings settings = settingsService.get(rootFolder);
            if(settings != null) {
                return new LegacyLanguageSpecConfig(settings);
            }
        }
        return config;
    }
}
