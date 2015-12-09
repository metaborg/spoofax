package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

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
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_NAME = "contributions.name";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_ID = "contributions.id";

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
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * Use the {@link ConfigurationBasedLanguageSpecConfigBuilder} instead.
     *
     * @param configuration The configuration that provides some of the properties.
     */
    protected ConfigurationBasedLanguageSpecConfig(
            final HierarchicalConfiguration<ImmutableNode> configuration,
            final LanguageIdentifier identifier,
            final String name,
            final Collection<LanguageIdentifier> compileDependencies,
            final Collection<LanguageIdentifier> runtimeDependencies,
            final Collection<LanguageContributionIdentifier> languageContributions,
            final Collection<String> pardonedLanguages
    ) {
        this(configuration);
        configuration.setProperty(PROP_NAME, name);
        configuration.setProperty(PROP_IDENTIFIER, identifier);
        configuration.setProperty(PROP_COMPILE_DEPENDENCIES, compileDependencies);
        configuration.setProperty(PROP_RUNTIME_DEPENDENCIES, runtimeDependencies);
        // TODO: languageContributions
        configuration.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
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
    @Override public Collection<LanguageContributionIdentifier> languageContributions() {
        // TODO: Implement!
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> pardonedLanguages() {
        return this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
    }

}
