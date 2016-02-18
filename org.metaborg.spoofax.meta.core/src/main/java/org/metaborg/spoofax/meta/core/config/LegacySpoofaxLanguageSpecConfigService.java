package org.metaborg.spoofax.meta.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.project.settings.ILegacySpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;

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


    @Override public boolean available(FileObject rootFolder) throws IOException {
        return languageSpecConfigService.available(rootFolder) || settingsService.available(rootFolder);
    }


    @Override public @Nullable ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException {
        final ISpoofaxLanguageSpecConfig config = languageSpecConfigService.get(languageSpec);
        if(config == null) {
            final LegacySpoofaxProjectSettings settings;
            try {
                settings = settingsService.get(languageSpec);
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
