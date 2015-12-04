package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageIdentifier;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface
 * that is backed by an {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedLanguageSpecConfig implements ILanguageSpecConfig, IConfigurationBasedConfig {

    private static final String PROP_IDENTIFIER = "identifier";
    private static final String PROP_NAME = "name";
    private static final String PROP_COMPILE_DEPENDENCIES = "compileDependencies";
    private static final String PROP_RUNTIME_DEPENDENCIES = "runtimeDependencies";
    private static final String PROP_PARDONED_LANGUAGES = "pardonedLanguages";

    protected final HierarchicalConfiguration<ImmutableNode> config;
    /**
     * {@inheritDoc}
     */
    @Override
    public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return this.config;
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * @param configuration The configuration that provides the properties.
     */
    public ConfigurationBasedLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration) {
        this.config = configuration;
    }


    /**
     * {@inheritDoc}
     */
    @Override public LanguageIdentifier identifier() {
        return this.config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
    }

    /**
     * {@inheritDoc}
     */
    @Override public String name() {
        return this.config.getString(PROP_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<LanguageIdentifier> compileDependencies() {
        return this.config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection<LanguageIdentifier> runtimeDependencies() {
        return this.config.getList(LanguageIdentifier.class, PROP_RUNTIME_DEPENDENCIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> pardonedLanguages() {
        return this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
    }

}
