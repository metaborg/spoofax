package org.metaborg.core.config;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class ProjectConfigBuilder extends AConfigBuilder implements IProjectConfigBuilder {
    protected @Nullable String metaborgVersion;
    protected @Nullable List<IExportConfig> sources;
    protected @Nullable Set<LanguageIdentifier> compileDeps;
    protected @Nullable Set<LanguageIdentifier> sourceDeps;
    protected @Nullable Set<LanguageIdentifier> javaDeps;


    @Inject public ProjectConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }

    @Override public IProjectConfig build(@Nullable FileObject rootFolder) {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }
        return build(configuration);
    }

    public ProjectConfig build(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ProjectConfig(configuration, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps);
    }


    @Override public IProjectConfigBuilder reset() {
        configuration = null;

        metaborgVersion = null;
        compileDeps = null;
        sourceDeps = null;
        javaDeps = null;
        return this;
    }

    @Override public IProjectConfigBuilder copyFrom(IProjectConfig config) {
        if(config instanceof IConfig) {
            this.configuration = cloneConfiguration((IConfig) config);
        } else {
            copyValuesFrom(config);
        }
        return this;
    }

    protected void copyValuesFrom(IProjectConfig config) {
        // Create a new configuration with the specified values.
        configuration = null;
        withMetaborgVersion(config.metaborgVersion());
        withCompileDeps(config.compileDeps());
        withSourceDeps(config.sourceDeps());
        withJavaDeps(config.javaDeps());
    }


    @Override public IProjectConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }

    @Override public IProjectConfigBuilder withSources(Iterable<IExportConfig> sources) {
        if(this.sources != null) {
            this.sources.clear();
        }

        addSources(sources);
        return this;
    }

    @Override public IProjectConfigBuilder addSources(Iterable<IExportConfig> sources) {
        if(this.sources == null) {
            this.sources = Lists.newArrayList();
        }

        Iterables.addAll(this.sources, sources);
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

}
