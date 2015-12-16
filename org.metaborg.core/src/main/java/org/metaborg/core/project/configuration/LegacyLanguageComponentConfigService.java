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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;

/**
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyLanguageComponentConfigService implements ILanguageComponentConfigService {

    private final ConfigurationBasedLanguageComponentConfigService configurationBasedLanguageComponentConfigService;
    private final IProjectSettingsService settingsService;
    private final ILanguageComponentConfigWriter configWriter;

    @Inject
    public LegacyLanguageComponentConfigService(final ConfigurationBasedLanguageComponentConfigService configurationBasedLanguageComponentConfigService,
                                                final IProjectSettingsService settingsService, final ILanguageComponentConfigWriter configWriter) {
        this.configurationBasedLanguageComponentConfigService = configurationBasedLanguageComponentConfigService;
        this.settingsService = settingsService;
        this.configWriter = configWriter;
    }

    @Nullable
    @Override
    public ILanguageComponentConfig get(final ILanguageComponent languageComponent) throws IOException {
        // Try get a configuration.
        @Nullable ILanguageComponentConfig config = this.configurationBasedLanguageComponentConfigService.get(languageComponent);

        // If this fails, try get project settings.
        if (config == null) {
            final IProjectSettings settings = this.settingsService.get(new IProject() {
                @Override
                public FileObject location() {
                    return languageComponent.location();
                }
            });
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacyLanguageComponentConfig(settings);
            }
        }
        return config;
    }

    @Nullable
    @Override
    public ILanguageComponentConfig get(FileObject rootFolder) throws IOException {
        // Try get a configuration.
        @Nullable ILanguageComponentConfig config = this.configurationBasedLanguageComponentConfigService.get(rootFolder);

        // If this fails, try get project settings.
        if (config == null) {
            final IProjectSettings settings = this.settingsService.get(rootFolder);
            if (settings != null) {
                // Convert the settings to a configuration
                config = new LegacyLanguageComponentConfig(settings);
            }
        }
        return config;
    }
}
