package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.settings.ILegacySpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.config.LegacySpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfigService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfigService implements ISpoofaxLanguageSpecConfigService {
    private final SpoofaxLanguageSpecConfigService languageSpecConfigService;
    private final ILegacySpoofaxProjectSettingsService settingsService;


    @Inject public LegacySpoofaxLanguageSpecConfigService(SpoofaxLanguageSpecConfigService languageSpecConfigService,
        ILegacySpoofaxProjectSettingsService settingsService) {
        this.languageSpecConfigService = languageSpecConfigService;
        this.settingsService = settingsService;
    }


    @Override public boolean available(FileObject rootFolder) {
        return languageSpecConfigService.available(rootFolder) || settingsService.available(rootFolder);
    }

    @Override public @Nullable ISpoofaxLanguageSpecConfig get(FileObject rootFolder) throws ConfigException {
        final ISpoofaxLanguageSpecConfig config = languageSpecConfigService.get(rootFolder);
        if(config == null) {
            final LegacySpoofaxProjectSettings settings;
            try {
                settings = settingsService.get(rootFolder);
            } catch(ProjectException e) {
                throw new ConfigException("Cannot retrieve legacy settings from " + rootFolder, e);
            }

            if(settings != null) {
                return new LegacySpoofaxLanguageSpecConfig(settings);
            }
        }
        return config;
    }
}
