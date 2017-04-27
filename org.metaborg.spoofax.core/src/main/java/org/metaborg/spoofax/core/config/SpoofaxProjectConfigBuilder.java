package org.metaborg.spoofax.core.config;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.ProjectConfigBuilder;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.nabl2.config.NaBL2Config;

import com.google.inject.Inject;

public class SpoofaxProjectConfigBuilder extends ProjectConfigBuilder implements ISpoofaxProjectConfigBuilder {
    protected @Nullable Boolean typesmart;
    protected @Nullable NaBL2Config nabl2Config;

    @Inject public SpoofaxProjectConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }

    @Override public ISpoofaxProjectConfig build(@Nullable FileObject rootFolder) {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }
        return new SpoofaxProjectConfig(configuration, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps,
                typesmart, nabl2Config);
    }

    public SpoofaxProjectConfig build(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new SpoofaxProjectConfig(configuration, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps,
                typesmart, nabl2Config);
    }

    @Override public ISpoofaxProjectConfigBuilder reset() {
        super.reset();
        typesmart = null;
        nabl2Config = null;
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder copyFrom(ISpoofaxProjectConfig config) {
        super.copyFrom(config);
        if(!(config instanceof IConfig)) {
            copyValuesFrom(config);
        }
        return this;
    }

    public void copyValuesFrom(ISpoofaxProjectConfig config) {
        super.copyValuesFrom(config);
        withTypesmart(config.typesmart());
        withNaBL2Config(config.nabl2Config());
    }

    @Override public ISpoofaxProjectConfigBuilder withMetaborgVersion(String metaborgVersion) {
        super.withMetaborgVersion(metaborgVersion);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder withSources(Iterable<IExportConfig> sources) {
        super.withSources(sources);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder addSources(Iterable<IExportConfig> sources) {
        super.addSources(sources);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.withCompileDeps(deps);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.addCompileDeps(deps);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.withSourceDeps(deps);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.addSourceDeps(deps);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.withJavaDeps(deps);
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.addJavaDeps(deps);
        return this;
    }



    @Override public ISpoofaxProjectConfigBuilder withTypesmart(boolean typesmart) {
        this.typesmart = typesmart;
        return this;
    }

    @Override public ISpoofaxProjectConfigBuilder withNaBL2Config(NaBL2Config config) {
        this.nabl2Config = config;
        return this;
    }

}