package org.metaborg.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
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

    protected final Set<LanguageIdentifier> compileDeps = Sets.newHashSet();
    protected final Set<LanguageIdentifier> sourceDeps = Sets.newHashSet();
    protected final Set<LanguageIdentifier> javaDeps = Sets.newHashSet();


    @Inject public ProjectConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        this.configReaderWriter = configReaderWriter;
    }


    @Override public IProjectConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        final JacksonConfiguration configuration = configReaderWriter.createConfiguration(null, rootFolder);

        return new ProjectConfig(configuration, compileDeps, sourceDeps, javaDeps);
    }

    @Override public IProjectConfigBuilder reset() {
        compileDeps.clear();
        sourceDeps.clear();
        javaDeps.clear();
        return this;
    }

    @Override public IProjectConfigBuilder copyFrom(IProjectConfig config) {
        withCompileDeps(config.compileDeps());
        withSourceDeps(config.sourceDeps());
        withJavaDeps(config.javaDeps());
        return this;
    }

    @Override public IProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        this.compileDeps.clear();
        addCompileDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.compileDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        this.sourceDeps.clear();
        addSourceDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.sourceDeps, deps);
        return this;
    }

    @Override public IProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        this.javaDeps.clear();
        addJavaDeps(deps);
        return this;
    }

    @Override public IProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.javaDeps, deps);
        return this;
    }
}
