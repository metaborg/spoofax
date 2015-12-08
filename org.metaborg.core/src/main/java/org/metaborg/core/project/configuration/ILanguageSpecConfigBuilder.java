package org.metaborg.core.project.configuration;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Builder for {@link ILanguageSpecConfig} objects.
 */
public interface ILanguageSpecConfigBuilder {

    /**
     * Builds the configuration.
     *
     * @return The built configuration.
     * @throws IllegalStateException An error occurred while
     *                               building the object.
     */
    ILanguageSpecConfig build() throws IllegalStateException;

    /**
     * Resets the values of this builder.
     *
     * @return This builder.
     */
    ILanguageSpecConfigBuilder reset();

    /**
     * Copies the values from the specified configuration.
     *
     * @param config The configuration to copy values from.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig config);

    /**
     * Sets the language identifier.
     *
     * @param identifier The language identifier.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * Sets the language name.
     *
     * @param name The language name.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withName(String name);

    /**
     * Sets the compile-time dependencies.
     *
     * @param dependencies The compile-time dependency identifiers.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds compile-time dependencies.
     *
     * @param dependencies The compile-time dependency identifiers.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Sets the runtime dependencies.
     *
     * @param dependencies The runtime dependency identifiers.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds runtime dependencies.
     *
     * @param dependencies The runtime dependency identifiers.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Sets the language contributions.
     *
     * @param contributions The language contributions.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withLanguageContributions(Iterable<LanguageContributionIdentifier> contributions);

    /**
     * Adds language contributions.
     *
     * @param contributions The language contributions.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder addLanguageContributions(Iterable<LanguageContributionIdentifier> contributions);

    /**
     * Sets the pardoned languages.
     *
     * @param contributions The language contributions.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> contributions);

    /**
     * Adds pardoned languages.
     *
     * @param contributions The language contributions.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> contributions);

}
