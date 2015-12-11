package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface
 * that is backed by an {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedLanguageComponentConfig implements ILanguageComponentConfig, IConfigurationBasedConfig {

    private static final String PROP_IDENTIFIER = "identifier";
    private static final String PROP_NAME = "name";
    private static final String PROP_COMPILE_DEPENDENCIES = "compileDependencies";
    private static final String PROP_RUNTIME_DEPENDENCIES = "runtimeDependencies";

    protected final HierarchicalConfiguration<ImmutableNode> config;
    /**
     * {@inheritDoc}
     */
    @Override
    public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return this.config;
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageComponentConfig} class.
     *
     * @param configuration The configuration that provides the properties.
     */
    public ConfigurationBasedLanguageComponentConfig(final HierarchicalConfiguration<ImmutableNode> configuration) {
        this.config = configuration;
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageComponentConfig} class.
     *
     * Use the {@link ConfigurationBasedLanguageSpecConfigBuilder} instead.
     *
     * @param configuration The configuration that provides some of the properties.
     */
    protected ConfigurationBasedLanguageComponentConfig(
            final HierarchicalConfiguration<ImmutableNode> configuration,
            final LanguageIdentifier identifier,
            final String name,
            final Collection<LanguageIdentifier> compileDependencies,
            final Collection<LanguageIdentifier> runtimeDependencies
    ) {
        this(configuration);
        configuration.setProperty(PROP_NAME, name);
        configuration.setProperty(PROP_IDENTIFIER, identifier);
        configuration.setProperty(PROP_COMPILE_DEPENDENCIES, compileDependencies);
        configuration.setProperty(PROP_RUNTIME_DEPENDENCIES, runtimeDependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override public LanguageIdentifier identifier() {
        @Nullable final LanguageIdentifier value = this.config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
        return value != null ? value : LanguageIdentifier.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String name() {
        @Nullable final String value = this.config.getString(PROP_NAME);
        return value != null ? value : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<LanguageIdentifier> compileDependencies() {
        @Nullable final List<LanguageIdentifier> value = this.config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection<LanguageIdentifier> runtimeDependencies() {
        @Nullable final List<LanguageIdentifier> value = this.config.getList(LanguageIdentifier.class, PROP_RUNTIME_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

}
