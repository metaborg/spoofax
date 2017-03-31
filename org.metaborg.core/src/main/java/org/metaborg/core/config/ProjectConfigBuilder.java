package org.metaborg.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class ProjectConfigBuilder implements IProjectConfigBuilder {
    protected final AConfigurationReaderWriter configReaderWriter;

    protected @Nullable HierarchicalConfiguration<ImmutableNode> configuration;

    protected @Nullable String metaborgVersion;
    protected @Nullable Set<LanguageIdentifier> compileDeps;
    protected @Nullable Set<LanguageIdentifier> sourceDeps;
    protected @Nullable Set<LanguageIdentifier> javaDeps;
    protected @Nullable Boolean typesmart;
    protected @Nullable Boolean nabl2Debug;
    protected @Nullable Boolean nabl2Incremental;


    @Inject public ProjectConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        this.configReaderWriter = configReaderWriter;
    }

    @Override public IProjectConfig build(@Nullable FileObject rootFolder) {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }
        return new ProjectConfig(configuration, metaborgVersion, compileDeps, sourceDeps, javaDeps, typesmart,
            nabl2Debug, nabl2Incremental);
    }

    @Override public IProjectConfigBuilder reset() {
        configuration = null;

        metaborgVersion = null;
        compileDeps = null;
        sourceDeps = null;
        javaDeps = null;
        typesmart = null;
        nabl2Debug = null;
        nabl2Incremental = null;
        return this;
    }

    @Override public IProjectConfigBuilder copyFrom(IProjectConfig config) {
        if(config instanceof IConfig) {
            // Clone configuration.
            final IConfig iconfig = (IConfig) config;
            final HierarchicalConfiguration<ImmutableNode> apacheConfig = iconfig.getConfig();
            final Configuration clonedConfig = ConfigurationUtils.cloneConfiguration(apacheConfig);
            @SuppressWarnings("unchecked") final HierarchicalConfiguration<ImmutableNode> clonedHierachicalConfig =
                (HierarchicalConfiguration<ImmutableNode>) clonedConfig;
            configuration = clonedHierachicalConfig;
        } else {
            // Create a new configuration with the specified values.
            configuration = null;
            withMetaborgVersion(config.metaborgVersion());
            withCompileDeps(config.compileDeps());
            withSourceDeps(config.sourceDeps());
            withJavaDeps(config.javaDeps());
            withTypesmart(config.typesmart());
            withNaBL2Incremental(config.nabl2Incremental());
        }

        return this;
    }


    @Override public IProjectConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }

    @Override public IProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        if(this.compileDeps != null) {
            this.compileDeps.clear();
        }

        addCompileDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        if(this.compileDeps == null) {
            this.compileDeps = Sets.newHashSet();
        }

        Iterables.addAll(this.compileDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        if(this.sourceDeps != null) {
            this.sourceDeps.clear();
        }

        addSourceDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        if(this.sourceDeps == null) {
            this.sourceDeps = Sets.newHashSet();
        }

        Iterables.addAll(this.sourceDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        if(this.javaDeps != null) {
            this.javaDeps.clear();
        }

        addJavaDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        if(this.javaDeps == null) {
            this.javaDeps = Sets.newHashSet();
        }

        Iterables.addAll(this.javaDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withTypesmart(boolean typesmart) {
        this.typesmart = typesmart;
        return this;
    }

    @Override public IProjectConfigBuilder withNaBL2Debug(boolean debug) {
        this.nabl2Debug = debug;
        return this;
    }

    @Override public IProjectConfigBuilder withNaBL2Incremental(boolean incremental) {
        this.nabl2Incremental = incremental;
        return this;
    }
}
