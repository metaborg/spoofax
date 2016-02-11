package org.metaborg.core.project.configuration;

import java.util.Collection;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object. {@link ILanguageComponentConfig} corresponds to {@link ILanguageSpecConfig},
 * so we can reuse the {@link ConfigurationBasedLanguageSpecConfig} implementation.
 */
public class ConfigurationBasedLanguageComponentConfig extends ConfigurationBasedLanguageSpecConfig implements
    ILanguageComponentConfig, IConfigurationBasedConfig {
    private static final long serialVersionUID = 33189118842345663L;


    public ConfigurationBasedLanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        super(configuration);
    }

    protected ConfigurationBasedLanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> configuration,
        LanguageIdentifier identifier, String name, Collection<LanguageIdentifier> compileDependencies,
        Collection<LanguageIdentifier> runtimeDependencies,
        Collection<LanguageContributionIdentifier> languageContributions) {
        super(configuration, identifier, name, compileDependencies, runtimeDependencies, languageContributions);
    }
}
