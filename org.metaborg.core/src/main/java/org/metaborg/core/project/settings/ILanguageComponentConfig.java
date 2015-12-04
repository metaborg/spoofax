package org.metaborg.core.project.settings;

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
     * Gets the runtime dependencies.
     *
     * @return An iterable of runtime dependency identifiers.
     */
    Iterable<LanguageIdentifier> runtimeDependencies();

}
