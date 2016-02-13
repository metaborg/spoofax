package org.metaborg.core.project.settings;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.configuration.ILanguageComponentConfig;

/**
 * @deprecated Use {@link ILanguageComponentConfig} instead.
 */
@Deprecated
public interface ILegacyProjectSettings {
    LanguageIdentifier identifier();

    String name();

    Iterable<LanguageIdentifier> compileDependencies();

    Iterable<LanguageIdentifier> runtimeDependencies();

    Iterable<LanguageContributionIdentifier> languageContributions();
}
