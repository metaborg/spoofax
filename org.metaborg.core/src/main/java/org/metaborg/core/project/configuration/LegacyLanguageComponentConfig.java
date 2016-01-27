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

import java.util.Collection;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.settings.ILegacyProjectSettings;

import com.google.common.collect.Lists;

@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageComponentConfig implements ILanguageComponentConfig {

    private final ILegacyProjectSettings settings;

    public LegacyLanguageComponentConfig(final ILegacyProjectSettings settings) {
        this.settings = settings;
    }


    @Override
    public LanguageIdentifier identifier() {
        return this.settings.identifier();
    }

    @Override
    public String name() {
        return this.settings.name();
    }

    @Override
    public Collection<LanguageIdentifier> compileDependencies() {
        return Lists.newArrayList(this.settings.compileDependencies());
    }

    @Override
    public Collection<LanguageIdentifier> runtimeDependencies() {
        return Lists.newArrayList(this.settings.runtimeDependencies());
    }
}