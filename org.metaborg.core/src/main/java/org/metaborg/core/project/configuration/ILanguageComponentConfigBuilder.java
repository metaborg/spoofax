package org.metaborg.core.project.configuration;

import org.metaborg.core.IObjectBuilder;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Builder for {@link ILanguageComponentConfig} objects.
 */
public interface ILanguageComponentConfigBuilder { //extends IObjectBuilder<ILanguageComponentConfig> {

    /**
     * Builds the object.
     *
     * @return The built object.
     * @throws IllegalStateException The builder state is not valid,
     * i.e. {@link #isValid()} returned <code>false</code>.
     */
    ILanguageComponentConfig build() throws IllegalStateException;

    /**
     * Determines whether the builder's state is valid.
     *
     * @return <code>true</code> when the builder's state is valid;
     * otherwise, <code>false</code>.
     */
    boolean isValid();

    /**
     * Resets the values of this builder.
     *
     * @return This builder.
     */
    ILanguageComponentConfigBuilder reset();

    /**
     * Copies the values from the specified object.
     *
     * @param obj The object to copy values from.
     */
    ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig obj);

    /**
     * Sets the language identifier.
     *
     * @param identifier The language identifier.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * Sets the language name.
     *
     * @param name The language name.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withName(String name);

    /**
     * Sets the compile-time dependencies.
     *
     * @param dependencies The compile-time dependency identifiers.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds compile-time dependencies.
     *
     * @param dependencies The compile-time dependency identifiers.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Sets the runtime dependencies.
     *
     * @param dependencies The runtime dependency identifiers.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds runtime dependencies.
     *
     * @param dependencies The runtime dependency identifiers.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies);


}
