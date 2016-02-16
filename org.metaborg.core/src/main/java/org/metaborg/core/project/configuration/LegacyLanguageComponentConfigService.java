/*
 * Copyright © 2015-2015
 * 
 * This file is part of Spoofax for IntelliJ.
 * 
 * Spoofax for IntelliJ is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Spoofax for IntelliJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Spoofax for IntelliJ. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageComponentConfigService implements ILanguageComponentConfigService {
    private final ConfigurationBasedLanguageComponentConfigService languageComponentConfigService;
    private final ILegacyProjectSettingsService settingsService;


    @Inject public LegacyLanguageComponentConfigService(
        ConfigurationBasedLanguageComponentConfigService languageComponentConfigService,
        ILegacyProjectSettingsService settingsService) {
        this.languageComponentConfigService = languageComponentConfigService;
        this.settingsService = settingsService;
    }


    @Nullable @Override public ILanguageComponentConfig get(ILanguageComponent languageComponent) throws IOException {
        ILanguageComponentConfig config = this.languageComponentConfigService.get(languageComponent);
        if(config == null) {
            final ILegacyProjectSettings settings = this.settingsService.get(languageComponent.location());
            if(settings != null) {
                config = new LegacyLanguageComponentConfig(settings);
            }
        }
        return config;
    }

    @Nullable @Override public ILanguageComponentConfig get(FileObject rootFolder) throws IOException {
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
