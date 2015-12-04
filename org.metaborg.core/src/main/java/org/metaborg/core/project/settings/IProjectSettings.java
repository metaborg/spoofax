package org.metaborg.core.project.settings;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * @deprecated Use {@link ILanguageComponentConfig} instead.
 */
@Deprecated
public interface IProjectSettings {
    public abstract LanguageIdentifier identifier();

    public abstract String name();

    public abstract Iterable<LanguageIdentifier> compileDependencies();

    public abstract Iterable<LanguageIdentifier> runtimeDependencies();

    public abstract Iterable<LanguageContributionIdentifier> languageContributions();
}
