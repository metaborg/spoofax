package org.metaborg.core.project.configuration;

import java.util.Collection;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.settings.ILegacyProjectSettings;

import com.google.common.collect.Lists;

@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfig implements ILanguageSpecConfig {
    private static final long serialVersionUID = 4321718437339177753L;
    public final ILegacyProjectSettings settings;


    public LegacyLanguageSpecConfig(final ILegacyProjectSettings settings) {
        this.settings = settings;
    }


    @Override public LanguageIdentifier identifier() {
        return this.settings.identifier();
    }

    @Override public String name() {
        return this.settings.name();
    }

    @Override public Collection<LanguageIdentifier> compileDependencies() {
        return Lists.newArrayList(this.settings.compileDependencies());
    }

    @Override public Collection<LanguageIdentifier> runtimeDependencies() {
        return Lists.newArrayList(this.settings.runtimeDependencies());
    }

    @Override public Collection<LanguageContributionIdentifier> languageContributions() {
        return Lists.newArrayList(this.settings.languageContributions());
    }

    @Override public String metaborgVersion() {
        return MetaborgConstants.METABORG_VERSION;
    }
}
