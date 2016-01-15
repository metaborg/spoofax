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

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;

import com.google.inject.Inject;

/**
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyLanguageSpecConfigService implements ILanguageSpecConfigService {

    private final ConfigurationBasedLanguageSpecConfigService configurationBasedLanguageSpecConfigService;
    private final IProjectSettingsService settingsService;
    private final ILanguageSpecConfigWriter configWriter;

    @Inject
    public LegacyLanguageSpecConfigService(final ConfigurationBasedLanguageSpecConfigService configurationBasedLanguageSpecConfigService,
            final IProjectSettingsService settingsService, final ILanguageSpecConfigWriter configWriter) {
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
        if (config == null) {
            final IProjectSettings settings = this.settingsService.get(languageSpec);
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacyLanguageSpecConfig(settings);

                // Write the configuration to file.
                this.configWriter.write(languageSpec, config, null);
            }
        }
        return config;
    }
}
