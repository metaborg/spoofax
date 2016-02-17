package org.metaborg.core.project.config;

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

    @Override public @Nullable ILanguageComponentConfig get(FileObject rootFolder) throws IOException {
        ILanguageComponentConfig config = this.languageComponentConfigService.get(rootFolder);
        if(config == null) {
            final ILegacyProjectSettings settings = this.settingsService.get(rootFolder);
            if(settings != null) {
                config = new LegacyLanguageComponentConfig(settings);
            }
        }
        return config;
    }
}
