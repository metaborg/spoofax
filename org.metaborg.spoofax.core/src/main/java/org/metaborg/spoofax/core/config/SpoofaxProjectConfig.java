package org.metaborg.spoofax.core.config;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.ProjectConfig;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.spoofax.core.build.SpoofaxDefaultSources;

public class SpoofaxProjectConfig extends ProjectConfig implements ISpoofaxProjectConfig {

    private static final String PROP_STR_TYPESMART = "debug.typesmart";

    private static final String PROP_RUNTIME = "runtime";

    public SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config, String metaborgVersion,
            Collection<IExportConfig> sources, Collection<LanguageIdentifier> compileDeps,
            Collection<LanguageIdentifier> sourceDeps, Collection<LanguageIdentifier> javaDeps, Boolean typesmart,
            IRuntimeConfig runtimeConfig) {
        super(config, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps);
        if(typesmart != null) {
            config.setProperty(PROP_STR_TYPESMART, typesmart);
        }
    }

    @Override public Collection<ISourceConfig> sources() {
        Collection<ISourceConfig> sources = super.sources();
        if(sources.isEmpty()) {
            return SpoofaxDefaultSources.DEFAULT_SPOOFAX_SOURCES;
        } else {
            return sources;
        }
    }

    @Override public boolean typesmart() {
        return config.getBoolean(PROP_STR_TYPESMART, false);
    }

    @Override public IRuntimeConfig runtimeConfig() {
        final HierarchicalConfiguration<ImmutableNode> config =
                Optional.ofNullable(configurationAt(PROP_RUNTIME, false)).orElse(new BaseHierarchicalConfiguration());
        return new RuntimeConfig(config);
    }

    @Override public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);
        return messages;
    }

}