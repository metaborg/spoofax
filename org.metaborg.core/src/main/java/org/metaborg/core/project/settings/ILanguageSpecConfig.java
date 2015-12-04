package org.metaborg.core.project.settings;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import java.util.Collection;

/**
 * Configuration of a language specification.
 */
public interface ILanguageSpecConfig {

    /**
     * Gets the language identifier.
     *
     * @return The language identifier.
     */
    LanguageIdentifier identifier();

    /**
     * Gets the language name.
     *
     * @return The language name.
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

    /**
     * Gets a sequence of languages whose errors are ignored.
     *
     * @return The pardoned languages.
     */
    Collection<String> pardonedLanguages();

}
