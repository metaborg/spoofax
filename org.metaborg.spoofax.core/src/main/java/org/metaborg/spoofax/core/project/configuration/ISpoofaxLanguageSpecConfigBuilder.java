package org.metaborg.spoofax.core.project.configuration;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.configuration.ILanguageSpecConfigBuilder;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.util.cmd.Arguments;

/**
 * Builder for {@link ISpoofaxLanguageSpecConfig} objects.
 */
public interface ISpoofaxLanguageSpecConfigBuilder extends ILanguageSpecConfigBuilder {
    /**
     * Builds the object.
     *
     * @param rootFolder
     *            The root folder.
     * @return The built object.
     * @throws IllegalStateException
     *             The builder state is not valid, i.e. {@link #isValid()} returned <code>false</code>.
     */
    ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * Determines whether the builder's state is valid.
     *
     * @return <code>true</code> when the builder's state is valid; otherwise, <code>false</code>.
     */
    boolean isValid();

    /**
     * Resets the values of this builder.
     *
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder reset();

    /**
     * Copies the values from the specified object.
     *
     * @param obj
     *            The object to copy values from.
     */
    ISpoofaxLanguageSpecConfigBuilder copyFrom(ISpoofaxLanguageSpecConfig obj);

    /**
     * Sets the language identifier.
     *
     * @param identifier
     *            The language identifier.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * Sets the language name.
     *
     * @param name
     *            The language name.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withName(String name);

    /**
     * Sets the compile-time dependencies.
     *
     * @param dependencies
     *            The compile-time dependency identifiers.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds compile-time dependencies.
     *
     * @param dependencies
     *            The compile-time dependency identifiers.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * Sets the runtime dependencies.
     *
     * @param dependencies
     *            The runtime dependency identifiers.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * Adds runtime dependencies.
     *
     * @param dependencies
     *            The runtime dependency identifiers.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * Sets the language contributions.
     *
     * @param contributions
     *            The language contributions.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contributions);

    /**
     * Adds language contributions.
     *
     * @param contributions
     *            The language contributions.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contributions);

    /**
     * Sets the pardoned languages.
     *
     * @param contributions
     *            The language contributions.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> contributions);

    /**
     * Adds pardoned languages.
     *
     * @param contributions
     *            The language contributions.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> contributions);

    /**
     * Sets the project artifact format.
     *
     * @param format
     *            A member of the {@link Format} enumeration.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withFormat(Format format);

    /**
     * Sets the external def.
     *
     * @param def
     *            The external def.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withExternalDef(String def);

    /**
     * Sets the external JAR.
     *
     * @param jar
     *            The external JAR.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withExternalJar(String jar);

    /**
     * Sets the external JAR flags.
     *
     * @param flags
     *            The external JAR flags.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withExternalJarFlags(String flags);

    /**
     * Sets the SDF arguments.
     *
     * @param args
     *            An iterable of SDF arguments.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfArgs(Arguments args);

    /**
     * Sets the Stratego arguments.
     *
     * @param args
     *            The Stratego arguments.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrategoArgs(Arguments args);
}
