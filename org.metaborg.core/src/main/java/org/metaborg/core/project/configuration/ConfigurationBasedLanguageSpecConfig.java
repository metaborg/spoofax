package org.metaborg.core.project.configuration;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface that is backed by an {@link ImmutableConfiguration}
 * object.
 */
public class ConfigurationBasedLanguageSpecConfig extends ConfigurationBasedLanguageComponentConfig implements
    ILanguageSpecConfig, IConfigurationBasedConfig {
    private static final long serialVersionUID = -7053551901853301773L;

    private static final String PROP_METABORG_VERSION = "metaborgVersion";


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * @param configuration
     *            The configuration that provides the properties.
     */
    public ConfigurationBasedLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        super(configuration);
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * Use the {@link ConfigurationBasedLanguageSpecConfigBuilder} instead.
     *
     * @param configuration
     *            The configuration that provides some of the properties.
     */
    protected ConfigurationBasedLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration,
        LanguageIdentifier identifier, String name, Collection<LanguageIdentifier> compileDependencies,
        Collection<LanguageIdentifier> runtimeDependencies,
        Collection<LanguageContributionIdentifier> languageContributions, String metaborgVersion) {
        this(configuration);

        configuration.setProperty(PROP_NAME, name);
        configuration.setProperty(PROP_IDENTIFIER, identifier);
        configuration.setProperty(PROP_COMPILE_DEPENDENCIES, compileDependencies);
        configuration.setProperty(PROP_SOURCE_DEPENDENCIES, runtimeDependencies);
        configuration.setProperty(PROP_METABORG_VERSION, metaborgVersion);

        for(LanguageContributionIdentifier lcid : languageContributions) {
            configuration.addProperty(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID, -1), lcid.identifier);
            configuration.addProperty(PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME, lcid.name);
        }
    }


    @Override public String metaborgVersion() {
        @Nullable final String value = this.config.getString(PROP_METABORG_VERSION);
        return value != null ? value : MetaborgConstants.METABORG_VERSION;
    }
}
