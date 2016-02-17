package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
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
     * {@inheritDoc}
     */
    boolean isValid();

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withName(String name);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contributions);

    /**
     * {@inheritDoc}
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
     *            A member of the {@link StrategoFormat} enumeration.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withFormat(StrategoFormat format);

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
