package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Builder for {@link ILanguageComponentConfig} objects.
 */
public interface ILanguageComponentConfigBuilder extends IProjectConfigBuilder {
    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * Determines whether the builder's state is valid.
     *
     * @return <code>true</code> when the builder's state is valid; otherwise, <code>false</code>.
     */
    boolean isValid();

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder reset();

    /**
     * Copies the values from the specified configuration.
     *
     * @param config
     *            The configuration to copy values from.
     */
    ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config);


    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder withMetaborgVersion(String metaborgVersion);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageComponentConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps);


    /**
     * Sets the language identifier.
     *
     * @param identifier
     *            The language identifier.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * Sets the language name.
     *
     * @param name
     *            The language name.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withName(String name);

    /**
     * Sets the language contributions.
     *
     * @param contribs
     *            The language contributions.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * Adds language contributions.
     *
     * @param contribs
     *            The language contributions.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * Sets the languages for while files are generated.
     *
     * @param generates
     *            The languages for while files are generated.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withGenerates(Iterable<IGenerateConfig> generates);

    /**
     * Adds languages for while files are generated.
     *
     * @param generates
     *            The languages for while files are generated.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder addGenerates(Iterable<IGenerateConfig> generates);

    /**
     * Sets the file exports.
     *
     * @param exports
     *            The file exports.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withExports(Iterable<IExportConfig> exports);

    /**
     * Sets the typesmart property.
     *
     * @param typesmart
     *            The typesmart property.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder withTypesmart(boolean typesmart);

    /**
     * Adds file exports.
     *
     * @param exports
     *            The file exports.
     * @return This builder.
     */
    ILanguageComponentConfigBuilder addExports(Iterable<IExportConfig> exports);
}
