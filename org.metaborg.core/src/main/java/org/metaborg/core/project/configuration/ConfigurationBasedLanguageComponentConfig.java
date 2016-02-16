package org.metaborg.core.project.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    protected static final String PROP_COMPILE_DEPENDENCIES = "dependencies.compile";
    protected static final String PROP_SOURCE_DEPENDENCIES = "dependencies.source";
    protected static final String PROP_JAVA_DEPENDENCIES = "dependencies.java";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME = "contributions(%d).name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID = "contributions(%d).id";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME = "contributions.name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID = "contributions.id";
    protected static final String PROP_GENERATES = "generates";
    protected static final String PROP_EXPORTS = "exports";

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
     * @param config
     *            The configuration that provides some of the properties.
     */
    protected ConfigurationBasedLanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> config,
        LanguageIdentifier identifier, String name, Collection<LanguageIdentifier> compileDeps,
        Collection<LanguageIdentifier> sourceDeps, Collection<LanguageContributionIdentifier> languageContributions) {
        this(config);

        config.setProperty(PROP_NAME, name);
        config.setProperty(PROP_IDENTIFIER, identifier);
        config.setProperty(PROP_COMPILE_DEPENDENCIES, compileDeps);
        config.setProperty(PROP_SOURCE_DEPENDENCIES, sourceDeps);

        for(LanguageContributionIdentifier lcid : languageContributions) {
            config.addProperty(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID, -1), lcid.identifier);
            config.addProperty(PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME, lcid.name);
        }
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return this.config;
    }


    @Override public LanguageIdentifier identifier() {
        final LanguageIdentifier value = config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
        return value != null ? value : LanguageIdentifier.EMPTY;
    }

    @Override public String name() {
        final String value = config.getString(PROP_NAME);
        return value != null ? value : "";
    }

    @Override public Collection<LanguageIdentifier> compileDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageIdentifier> sourceDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_SOURCE_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageIdentifier> javaDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_JAVA_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageContributionIdentifier> langContribs() {
        final List<LanguageIdentifier> ids =
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

    @Override public Collection<Generate> generates() {
        final List<Generate> generates = config.getList(Generate.class, PROP_JAVA_DEPENDENCIES);
        return generates != null ? generates : Collections.<Generate>emptyList();
    }

    @Override public Collection<Export> exports() {
        final List<Export> exports = config.getList(Export.class, PROP_JAVA_DEPENDENCIES);
        return exports != null ? exports : Collections.<Export>emptyList();
    }
}
