package org.metaborg.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import javax.annotation.Nullable;

/**
 * Builder for {@link ILanguageSpecConfig} objects.
 */
public interface ILanguageSpecConfigBuilder { //extends IObjectBuilder<ILanguageSpecConfig> {

    /**
     * Builds the object.
     *
     * @param rootFolder The root folder.
     * @return The built object.
     * @throws IllegalStateException The builder state is not valid,
     * i.e. {@link #isValid()} returned <code>false</code>.
     */
    ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

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
    ILanguageSpecConfigBuilder reset();

    /**
     * Copies the values from the specified object.
     *
     * @param obj The object to copy values from.
     */
    ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig obj);

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

}
