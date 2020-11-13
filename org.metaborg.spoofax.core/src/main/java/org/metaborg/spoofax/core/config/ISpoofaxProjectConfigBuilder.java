package org.metaborg.spoofax.core.config;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IProjectConfigBuilder;
import org.metaborg.core.language.LanguageIdentifier;

import mb.nabl2.config.NaBL2Config;

public interface ISpoofaxProjectConfigBuilder extends IProjectConfigBuilder {

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfig build(@Nullable FileObject rootFolder);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder reset();

    /**
     * Copies the values from the specified configuration.
     *
     * @param config
     *            The configuration to copy values from.
     */
    ISpoofaxProjectConfigBuilder copyFrom(ISpoofaxProjectConfig config);


    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder withMetaborgVersion(String metaborgVersion);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder withSources(Iterable<IExportConfig> sources);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder addSources(Iterable<IExportConfig> sources);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps);

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps);


    /**
     * Sets whether debug output is enabled for the constraint solver.
     * 
     * @param config
     *            NaBL2 configuration.
     * @return This builder.
     */
    ISpoofaxProjectConfigBuilder withNaBL2Config(NaBL2Config config);

    /**
     * Set names languages for which Statix should use the concurrent solver.
     */
    ISpoofaxProjectConfigBuilder withStatixConcurrentLanguages(Iterable<String> langs);

    /**
     * Add names of languages for which Statix should use the concurrent solver.
     */
    ISpoofaxProjectConfigBuilder addStatixConcurrentLanguages(Iterable<String> langs);

}