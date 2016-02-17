package org.metaborg.core.project.config;

import org.metaborg.core.language.LanguageIdentifier;

/**
 * Configuration of a language component at runtime. Composition of multiple {@link ILanguageComponentConfig}
 * configurations.
 */
public interface ILanguageImplConfig {
    /**
     * Gets the compile dependencies.
     *
     * @return The compile dependency identifiers.
     */
    Iterable<LanguageIdentifier> compileDeps();

    /**
     * Gets the source dependencies.
     *
     * @return The source dependency identifiers.
     */
    Iterable<LanguageIdentifier> sourceDeps();

    /**
     * Gets the Java dependencies.
     *
     * @return The Java dependency identifiers.
     */
    Iterable<LanguageIdentifier> javaDeps();

    /**
     * Gets the languages for while files are generated.
     *
     * @return The languages for while files are generated.
     */
    Iterable<Generate> generates();

    /**
     * Gets the file exports.
     *
     * @return The file exports.
     */
    Iterable<Export> exports();
}
