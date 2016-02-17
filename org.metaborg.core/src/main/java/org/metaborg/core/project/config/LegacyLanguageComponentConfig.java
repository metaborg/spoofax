package org.metaborg.core.project.config;

import java.util.Collection;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.settings.ILegacyProjectSettings;

import com.google.common.collect.Lists;

@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageComponentConfig implements ILanguageComponentConfig {
    private final ILegacyProjectSettings settings;


    public LegacyLanguageComponentConfig(ILegacyProjectSettings settings) {
        this.settings = settings;
    }


    @Override public LanguageIdentifier identifier() {
        return this.settings.identifier();
    }

    @Override public String name() {
        return this.settings.name();
    }

    @Override public Collection<LanguageIdentifier> compileDeps() {
        return Lists.newArrayList(settings.compileDependencies());
    }

    @Override public Collection<LanguageIdentifier> sourceDeps() {
        return Lists.newArrayList(settings.runtimeDependencies());
    }

    @Override public Collection<LanguageIdentifier> javaDeps() {
        return Lists.newArrayList();
    }

    @Override public Collection<LanguageContributionIdentifier> langContribs() {
        return Lists.newArrayList(settings.languageContributions());
    }

    @Override public Collection<Generate> generates() {
        return Lists.newArrayList();
    }

    @Override public Collection<Export> exports() {
        return Lists.newArrayList();
    }
}
