/*
 * Copyright © 2015-2015
 *
 * This file is part of Spoofax for IntelliJ.
 *
 * Spoofax for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoofax for IntelliJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Spoofax for IntelliJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;

import com.google.inject.Inject;

/**
 * This class is only used for the configuration system migration.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfigService implements ILanguageSpecConfigService {

    private final ConfigurationBasedLanguageSpecConfigService configurationBasedLanguageSpecConfigService;
    private final ILegacyProjectSettingsService settingsService;
    private final ILanguageSpecConfigWriter configWriter;

    @Inject
    public LegacyLanguageSpecConfigService(final ConfigurationBasedLanguageSpecConfigService configurationBasedLanguageSpecConfigService,
                                           final ILegacyProjectSettingsService settingsService, final ILanguageSpecConfigWriter configWriter) {
        this.configurationBasedLanguageSpecConfigService = configurationBasedLanguageSpecConfigService;
        this.settingsService = settingsService;
        this.configWriter = configWriter;
    }

    @Nullable
    @Override
    public ILanguageSpecConfig get(final ILanguageSpec languageSpec) throws IOException {
        // Try get a configuration.
        @Nullable ILanguageSpecConfig config = this.configurationBasedLanguageSpecConfigService.get(languageSpec);

        // If this fails, try get project settings.
        if (config == null && languageSpec instanceof IProject) {
            @Nullable final ILegacyProjectSettings settings;
            settings = this.settingsService.get((IProject) languageSpec);
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacyLanguageSpecConfig(settings);

//                // Write the configuration to file.
//                // FIXME: This is only for migrating the old settings system to the new.
//                this.configWriter.write(languageSpec, config, null);
            }
        }
        return config;
    }
}
