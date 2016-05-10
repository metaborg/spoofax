package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
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
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> contributions);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> contributions);

    /**
     * {@inheritDoc}
     */
    ISpoofaxLanguageSpecConfigBuilder withUseBuildSystemSpec(boolean useBuildSystemSpec);

    /**
     * Sets the SDF version.
     *
     * @param version
     *            The SDF version.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfVersion(SdfVersion version);
    
    /**
     * Sets the placeholder prefix.
     *
     * @param placeholderPrefix
     *            The placeholder prefix.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withPlaceholderPrefix(String placeholderPrefix);
    
    /**
     * Sets the placeholder postfix.
     *
     * @param placeholderPostfix
     *            The placeholder postfix.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withPlaceholderPostfix(String placeholderPostfix);

    /**
     * Sets the external def.
     *
     * @param def
     *            The external def.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfExternalDef(String def);

    /**
     * Sets the SDF arguments.
     *
     * @param args
     *            An iterable of SDF arguments.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfArgs(Arguments args);

    /**
     * Sets the project artifact format.
     *
     * @param format
     *            A member of the {@link StrategoFormat} enumeration.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrFormat(StrategoFormat format);

    /**
     * Sets the external JAR.
     *
     * @param jar
     *            The external JAR.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrExternalJar(String jar);

    /**
     * Sets the external JAR flags.
     *
     * @param flags
     *            The external JAR flags.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrExternalJarFlags(String flags);

    /**
     * Sets the Stratego arguments.
     *
     * @param args
     *            The Stratego arguments.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrArgs(Arguments args);

    /**
     * Sets the build step configurations.
     * 
     * @param buildSteps
     *            Build step configurations.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withBuildSteps(Iterable<IBuildStepConfig> buildSteps);

    /**
     * Adds build step configurations.
     * 
     * @param buildSteps
     *            Build step configurations.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder addBuildSteps(Iterable<IBuildStepConfig> buildSteps);
}
