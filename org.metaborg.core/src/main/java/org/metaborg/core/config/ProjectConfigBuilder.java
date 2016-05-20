package org.metaborg.core.config;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class ProjectConfigBuilder implements IProjectConfigBuilder {
    protected final AConfigurationReaderWriter configReaderWriter;

    protected @Nullable String metaborgVersion = MetaborgConstants.METABORG_VERSION;
    protected @Nullable HierarchicalConfiguration<ImmutableNode> configuration;
    protected @Nullable Set<LanguageIdentifier> compileDeps;
    protected @Nullable Set<LanguageIdentifier> sourceDeps;
    protected @Nullable Set<LanguageIdentifier> javaDeps;
    protected @Nullable Boolean typesmart;

    @Inject public ProjectConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        this.configReaderWriter = configReaderWriter;
    }

    @Override public IProjectConfig build(@Nullable FileObject rootFolder) {
        if (this.configuration == null)
            this.configuration = configReaderWriter.create(null, rootFolder);

        return new ProjectConfig(configuration, metaborgVersion, compileDeps, sourceDeps, javaDeps, typesmart);
    }

    @Override public IProjectConfigBuilder reset() {
        this.metaborgVersion = null;//MetaborgConstants.METABORG_VERSION;
        this.configuration = null;
        this.compileDeps = null;
        this.sourceDeps = null;
        this.javaDeps = null;
        this.typesmart = null;
        return this;
    }

    @Override public IProjectConfigBuilder copyFrom(IProjectConfig config) {
        if (config instanceof IConfig) {
            // Clone the given configuration.
            this.configuration = (HierarchicalConfiguration<ImmutableNode>)ConfigurationUtils
                    .cloneConfiguration(((IConfig)config).getConfig());
        } else {
            // Create a new configuration with the specified values.
            this.configuration = null;
            withMetaborgVersion(config.metaborgVersion());
            withCompileDeps(config.compileDeps());
            withSourceDeps(config.sourceDeps());
            withJavaDeps(config.javaDeps());
            withTypesmart(config.typesmart());
        }

        return this;
    }


    @Override public IProjectConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }

    @Override public IProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        if (this.compileDeps != null)
            this.compileDeps.clear();

        addCompileDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        if (this.compileDeps == null)
            this.compileDeps = Sets.newHashSet();

        Iterables.addAll(this.compileDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        if (this.sourceDeps != null)
            this.sourceDeps.clear();

        addSourceDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        if (this.sourceDeps == null)
            this.sourceDeps = Sets.newHashSet();

        Iterables.addAll(this.sourceDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        if (this.javaDeps != null)
            this.javaDeps.clear();

        addJavaDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder withTypesmart(boolean typesmart) {
        this.typesmart = typesmart;
        return this;
    }

    @Override public IProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        if (this.javaDeps == null)
            this.javaDeps = Sets.newHashSet();

        Iterables.addAll(this.javaDeps, deps);
        return this;
    }
}
