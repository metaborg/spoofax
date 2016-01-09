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

package org.metaborg.spoofax.core.project.configuration;

import javax.annotation.Nullable;

import org.metaborg.core.project.configuration.LegacyLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

@Deprecated
public class LegacySpoofaxLanguageSpecConfig extends LegacyLanguageSpecConfig implements ISpoofaxLanguageSpecConfig {

    private final SpoofaxProjectSettings settings;

    public LegacySpoofaxLanguageSpecConfig(final SpoofaxProjectSettings settings) {
        super(settings.settings());
        this.settings = settings;
    }


    @Override
    public Format format() {
        return this.settings.format();
    }

    @Override
    public Iterable<String> sdfArgs() {
        return this.settings.sdfArgs();
    }

    @Override
    public Iterable<String> strategoArgs() {
        return this.settings.strategoArgs();
    }

    @Nullable
    @Override
    public String externalDef() {
        return this.settings.externalDef();
    }

    @Nullable
    @Override
    public String externalJar() {
        return this.settings.externalJar();
    }

    @Nullable
    @Override
    public String externalJarFlags() {
        return this.settings.externalJarFlags();
    }

    @Override
    public String strategoName() {
        return this.settings.strategoName();
    }

    @Override
    public String javaName() {
        return this.settings.javaName();
    }

    @Override
    public String packageName() {
        return this.settings.packageName();
    }

    @Override
    public String packagePath() {
        return this.settings.packagePath();
    }
}
