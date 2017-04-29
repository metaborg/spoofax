package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.nabl2.config.NaBL2Config;
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
    @Override ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder reset();

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
    @Override ISpoofaxLanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> dependencies);

    /**
     * {@see ISpoofaxProjectConfigBuilder#withTypesmart(boolean)}
     */
    ISpoofaxLanguageSpecConfigBuilder withTypesmart(boolean typesmart);

    /**
     * {@see ISpoofaxProjectConfigBuilder#withNaBL2Config(NaBL2Config)}
     */
    ISpoofaxLanguageSpecConfigBuilder withNaBL2Config(NaBL2Config config);


    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withName(String name);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withGenerates(Iterable<IGenerateConfig> generates);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addGenerates(Iterable<IGenerateConfig> generates);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withExports(Iterable<IExportConfig> exports);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addExports(Iterable<IExportConfig> exports);


    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxLanguageSpecConfigBuilder withUseBuildSystemSpec(boolean useBuildSystemSpec);


    /**
     * Sets the SDF version.
     *
     * @param sdfVersion
     *            The SDF version.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfVersion(SdfVersion sdfversion);

    /**
     * Sets the sdf2table version.
     *
     * @param sdf2tableVersion
     *            The sdf2table version.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdf2tableVersion(Sdf2tableVersion sdf2tableVersion);

    /**
     * Sets the path to the main SDF file.
     *
     * @param sdfMainFile
     *            The path to the main SDF file.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withSdfMainFile(String sdfMainFile);

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
     * Sets the language to be pretty printed.
     *
     * @param prettyPrintLanguage
     *            The language to be pretty printed.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withPrettyPrintLanguage(String prettyPrintLanguage);

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
     * Sets the Stratego typesmart property.
     *
     * @param args
     *            The Stratego typesmart property.
     * @return This builder.
     */
    ISpoofaxLanguageSpecConfigBuilder withStrTypesmart(boolean typesmart);

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
