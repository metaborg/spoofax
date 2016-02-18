package org.metaborg.meta.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.Export;
import org.metaborg.core.config.Generate;
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
    ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException;

    /**
     * {@inheritDoc}
     */
    boolean isValid();

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder reset();

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
    ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withName(String name);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contribs);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withGenerates(Iterable<Generate> generates);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addGenerates(Iterable<Generate> generates);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder withExports(Iterable<Export> exports);

    /**
     * {@inheritDoc}
     */
    ILanguageSpecConfigBuilder addExports(Iterable<Export> exports);

    /**
     * Sets the MetaBorg version.
     *
     * @param metaborgVersion
     *            The MetaBorg version.
     * @return This builder.
     */
    ILanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion);
}
