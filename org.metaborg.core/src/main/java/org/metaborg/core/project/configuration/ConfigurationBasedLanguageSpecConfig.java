package org.metaborg.core.project.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ILanguageSpecConfig} interface that is backed by an {@link ImmutableConfiguration}
 * object.
 */
public class ConfigurationBasedLanguageSpecConfig implements ILanguageSpecConfig, IConfigurationBasedConfig {
    private static final long serialVersionUID = -7053551901853301773L;

    private static final String PROP_IDENTIFIER = "id";
    private static final String PROP_NAME = "name";
    private static final String PROP_COMPILE_DEPENDENCIES = "compileDependencies";
    private static final String PROP_RUNTIME_DEPENDENCIES = "runtimeDependencies";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME = "contributions(%d).name";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID = "contributions(%d).id";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME = "contributions.name";
    private static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID = "contributions.id";
    private static final String PROP_METABORG_VERSION = "metaborgVersion";

    protected final HierarchicalConfiguration<ImmutableNode> config;


    /**
     * {@inheritDoc}
     */
    @Override public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return this.config;
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfig} class.
     *
     * @param configuration
     *            The configuration that provides the properties.
     */
    public ConfigurationBasedLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration) {
        Preconditions.checkNotNull(configuration);

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
    protected ConfigurationBasedLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration,
        final LanguageIdentifier identifier, final String name,
        final Collection<LanguageIdentifier> compileDependencies,
        final Collection<LanguageIdentifier> runtimeDependencies,
        final Collection<LanguageContributionIdentifier> languageContributions, final String metaborgVersion) {
        this(configuration);

        configuration.setProperty(PROP_NAME, name);
        configuration.setProperty(PROP_IDENTIFIER, identifier);
        configuration.setProperty(PROP_COMPILE_DEPENDENCIES, compileDependencies);
        configuration.setProperty(PROP_RUNTIME_DEPENDENCIES, runtimeDependencies);
        configuration.setProperty(PROP_METABORG_VERSION, metaborgVersion);

        for(LanguageContributionIdentifier lcid : languageContributions) {
            configuration.addProperty(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID, -1), lcid.identifier);
            configuration.addProperty(PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME, lcid.name);
        }
    }


    /**
     * {@inheritDoc}
     *
     * The identifier is stored at the <code>id</code> element. An identifier has the syntax
     * <code>groupid:artifactid:version</code>.
     */
    @Override public LanguageIdentifier identifier() {
        @Nullable final LanguageIdentifier value = this.config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
        return value != null ? value : LanguageIdentifier.EMPTY;
    }

    /**
     * {@inheritDoc}
     *
     * The name is stored at the <code>name</code> element.
     */
    @Override public String name() {
        @Nullable final String value = this.config.getString(PROP_NAME);
        return value != null ? value : "";
    }

    /**
     * {@inheritDoc}
     *
     * The compile dependencies are stored as a list at the <code>compileDependencies</code> element. Each compile
     * dependency is an identifier with the syntax <code>groupid:artifactid:version</code>.
     */
    @Override public Collection<LanguageIdentifier> compileDependencies() {
        @Nullable final List<LanguageIdentifier> value =
            this.config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * The runtime dependencies are stored as a list at the <code>runtimeDependencies</code> element. Each runtime
     * dependency is an identifier with the syntax <code>groupid:artifactid:version</code>.
     */
    @Override public Collection<LanguageIdentifier> runtimeDependencies() {
        @Nullable final List<LanguageIdentifier> value =
            this.config.getList(LanguageIdentifier.class, PROP_RUNTIME_DEPENDENCIES);
        return value != null ? value : Collections.<LanguageIdentifier>emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * The language contributions are stored as a list at the <code>contributions</code> element. Each language
     * contributions is a name at <code>name</code>, and an an identifier at <code>id</code> with the syntax
     * <code>groupid:artifactid:version</code>.
     */
    @Override public Collection<LanguageContributionIdentifier> languageContributions() {
        @Nullable final List<LanguageIdentifier> ids =
            this.config.getList(LanguageIdentifier.class, PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID);
        if(ids == null) {
            return Lists.newArrayList();
        }

        final List<LanguageContributionIdentifier> lcids = new ArrayList<>(ids.size());
        for(int i = 0; i < ids.size(); i++) {
            LanguageIdentifier identifier = ids.get(i);
            String name = this.config.getString(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME, i));
            lcids.add(new LanguageContributionIdentifier(identifier, name));
        }
        return lcids;
    }

    @Override public String metaborgVersion() {
        @Nullable final String value = this.config.getString(PROP_METABORG_VERSION);
        return value != null ? value : MetaborgConstants.METABORG_VERSION;
    }
}
