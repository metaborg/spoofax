package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Builder for {@link ILanguageComponentConfig} objects.
 */
public interface IProjectConfigBuilder {
    /**
     * Builds the object.
     *
     * @param rootFolder
     *            The root folder.
     * @return The built object.
     * @throws IllegalStateException
     *             The builder state is not valid, i.e. {@link #isValid()} returned <code>false</code>.
     */
    IProjectConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * Resets the values of this builder.
     *
     * @return This builder.
     */
    IProjectConfigBuilder reset();

    /**
     * Copies the values from the specified configuration.
     *
     * @param config
     *            The configuration to copy values from.
     */
    IProjectConfigBuilder copyFrom(IProjectConfig config);

    /**
     * Sets the compile-time dependencies.
     *
     * @param deps
     *            The compile-time dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * Adds compile dependencies.
     *
     * @param deps
     *            The compile dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * Sets the source dependencies.
     *
     * @param deps
     *            The source dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * Adds source dependencies.
     *
     * @param deps
     *            The source dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * Sets the java dependencies.
     *
     * @param deps
     *            The java dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * Adds java dependencies.
     *
     * @param deps
     *            The java dependency identifiers.
     * @return This builder.
     */
    IProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps);
}
