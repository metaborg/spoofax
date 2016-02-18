package org.metaborg.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageComponentConfigService implements ILanguageComponentConfigService {
    private final LanguageComponentConfigService languageComponentConfigService;
    private final ILegacyProjectSettingsService settingsService;


    @Inject public LegacyLanguageComponentConfigService(LanguageComponentConfigService languageComponentConfigService,
        ILegacyProjectSettingsService settingsService) {
        this.languageComponentConfigService = languageComponentConfigService;
        this.settingsService = settingsService;
    }

    
    @Override public boolean available(FileObject rootFolder) throws IOException {
        return languageComponentConfigService.available(rootFolder);
    }

    @Override public @Nullable ILanguageComponentConfig get(FileObject rootFolder) throws IOException {
        final ILanguageComponentConfig config = languageComponentConfigService.get(rootFolder);
        if(config == null) {
            final ILegacyProjectSettings settings = settingsService.get(rootFolder);
            if(settings != null) {
                return new LegacyLanguageComponentConfig(settings);
            }
        }
        return config;
    }
}
