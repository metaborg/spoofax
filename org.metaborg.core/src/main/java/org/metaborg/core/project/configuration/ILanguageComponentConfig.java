package org.metaborg.core.project.configuration;

import java.util.Collection;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Configuration of a language component at runtime.
 * 
 * To create a new instance of this interface, use an {@link ILanguageComponentConfigBuilder} interface.
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
     * Gets the compile dependencies.
     *
     * @return The compile dependency identifiers.
     */
    Collection<LanguageIdentifier> compileDeps();

    /**
     * Gets the source dependencies.
     *
     * @return The source dependency identifiers.
     */
    Collection<LanguageIdentifier> sourceDeps();
    
    /**
     * Gets the Java dependencies.
     *
     * @return The Java dependency identifiers.
     */
    Collection<LanguageIdentifier> javaDeps();

    /**
     * Gets the language contributions.
     *
     * @return The language contributions.
     */
    Collection<LanguageContributionIdentifier> langContribs();
    
    /**
     * Gets the languages for while files are generated.
     *
     * @return The languages for while files are generated.
     */
    Collection<Generate> generates();
    
    /**
     * Gets the file exports.
     *
     * @return The file exports.
     */
    Collection<Export> exports();
}
