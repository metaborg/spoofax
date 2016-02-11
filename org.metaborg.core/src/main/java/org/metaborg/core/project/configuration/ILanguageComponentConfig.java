package org.metaborg.core.project.configuration;

import java.util.Collection;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Configuration of a language component.
 */
public interface ILanguageComponentConfig {

    /**
     * Gets the language identifier.
     *
     * @return The language identifier.
     */
    LanguageIdentifier identifier();

    /**
     * Gets the language name.
     *
     * @return The name.
     */
    String name();

    /**
     * Gets the compile-time dependencies.
     *
     * @return The compile-time dependency identifiers.
     */
    Collection<LanguageIdentifier> compileDependencies();

    /**
     * Gets the runtime dependencies.
     *
     * @return The runtime dependency identifiers.
     */
    Collection<LanguageIdentifier> runtimeDependencies();

    /**
     * Gets the language contributions.
     *
     * @return The language contributions.
     */
    Collection<LanguageContributionIdentifier> languageContributions();
}
