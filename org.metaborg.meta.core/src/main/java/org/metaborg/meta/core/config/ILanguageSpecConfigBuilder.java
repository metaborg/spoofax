package org.metaborg.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.ILanguageComponentConfigBuilder;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Builder for {@link ILanguageSpecConfig} objects.
 */
public interface ILanguageSpecConfigBuilder extends ILanguageComponentConfigBuilder {
    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder reset();

    /**
     * Copies the values from the specified configuration.
     *
     * @param config
     *            The configuration to copy values from.
     */
    ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig obj);


    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps);


    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withName(String name);
    
    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withGenerates(Iterable<IGenerateConfig> generates);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addGenerates(Iterable<IGenerateConfig> generates);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder withExports(Iterable<IExportConfig> exports);

    /**
     * {@inheritDoc}
     */
    @Override ILanguageSpecConfigBuilder addExports(Iterable<IExportConfig> exports);


    /**
     * Sets the pardoned languages.
     *
     * @param languages
     *            The pardoned languages.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages);

    /**
     * Adds pardoned languages.
     *
     * @param languages
     *            The pardoned languages.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages);

    /**
     * Sets the flag to use the build system specification.
     * 
     * @param useBuildSystemSpec
     *            True to use the build system specification, false otherwise.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withUseBuildSystemSpec(boolean useBuildSystemSpec);
}
