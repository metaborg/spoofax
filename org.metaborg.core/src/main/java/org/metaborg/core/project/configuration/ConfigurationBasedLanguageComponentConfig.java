package org.metaborg.core.project.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedLanguageComponentConfig implements ILanguageComponentConfig, IConfigurationBasedConfig {
    protected static final String PROP_IDENTIFIER = "id";
    protected static final String PROP_NAME = "name";
    protected static final String PROP_COMPILE_DEPENDENCIES = "compileDependencies";
    protected static final String PROP_RUNTIME_DEPENDENCIES = "runtimeDependencies";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME = "contributions(%d).name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID = "contributions(%d).id";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME = "contributions.name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID = "contributions.id";

    protected final HierarchicalConfiguration<ImmutableNode> config;


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * @param configuration
     *            The configuration that provides the properties.
     */
    public ConfigurationBasedLanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        this.config = configuration;
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * Use the {@link ConfigurationBasedLanguageSpecConfigBuilder} instead.
     *
     * @param configuration
     *            The configuration that provides some of the properties.
     */
    protected ConfigurationBasedLanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> configuration,
        LanguageIdentifier identifier, String name, Collection<LanguageIdentifier> compileDependencies,
        Collection<LanguageIdentifier> runtimeDependencies,
        Collection<LanguageContributionIdentifier> languageContributions) {
        this(configuration);

        configuration.setProperty(PROP_NAME, name);
        configuration.setProperty(PROP_IDENTIFIER, identifier);
        configuration.setProperty(PROP_COMPILE_DEPENDENCIES, compileDependencies);
        configuration.setProperty(PROP_RUNTIME_DEPENDENCIES, runtimeDependencies);

        for(LanguageContributionIdentifier lcid : languageContributions) {
            configuration.addProperty(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID, -1), lcid.identifier);
            configuration.addProperty(PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME, lcid.name);
        }
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return this.config;
    }


    @Override public LanguageIdentifier identifier() {
        final @Nullable LanguageIdentifier value = config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
        return value != null ? value : LanguageIdentifier.EMPTY;
    }

    @Override public String name() {
        final @Nullable String value = config.getString(PROP_NAME);
        return value != null ? value : "";
    }

    @Override public Collection<LanguageIdentifier> compileDependencies() {
        final @Nullable List<LanguageIdentifier> value =
            config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageIdentifier> runtimeDependencies() {
        final @Nullable List<LanguageIdentifier> value =
            config.getList(LanguageIdentifier.class, PROP_RUNTIME_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageContributionIdentifier> languageContributions() {
        final @Nullable List<LanguageIdentifier> ids =
            config.getList(LanguageIdentifier.class, PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID);
        if(ids == null) {
            return Lists.newArrayList();
        }

        final List<LanguageContributionIdentifier> lcids = new ArrayList<>(ids.size());
        for(int i = 0; i < ids.size(); i++) {
            LanguageIdentifier identifier = ids.get(i);
            String name = config.getString(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME, i));
            lcids.add(new LanguageContributionIdentifier(identifier, name));
        }
        return lcids;
    }
}
