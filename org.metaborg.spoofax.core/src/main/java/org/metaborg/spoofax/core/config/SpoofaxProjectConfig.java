package org.metaborg.spoofax.core.config;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.ProjectConfig;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.spoofax.core.build.SpoofaxDefaultSources;
import org.metaborg.spoofax.core.config.language.NaBL2ConfigReaderWriter;

public class SpoofaxProjectConfig extends ProjectConfig implements ISpoofaxProjectConfig {

    private static final String PROP_STR_TYPESMART = "debug.typesmart";

    private static final String PROP_RUNTIME = "runtime";
    private static final String PROP_NABL2 = PROP_RUNTIME + ".nabl2";

    public SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config, String metaborgVersion,
            Collection<IExportConfig> sources, Collection<LanguageIdentifier> compileDeps,
            Collection<LanguageIdentifier> sourceDeps, Collection<LanguageIdentifier> javaDeps, Boolean typesmart,
            NaBL2Config nabl2Config) {
        super(config, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps);
        if(typesmart != null) {
            config.setProperty(PROP_STR_TYPESMART, typesmart);
        }
        if(nabl2Config != null) {
            Optional.ofNullable(configurationAt(PROP_NABL2, true))
                    .ifPresent(c -> NaBL2ConfigReaderWriter.write(nabl2Config, c));
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

    @Override public NaBL2Config nabl2Config() {
        return Optional.ofNullable(configurationAt(PROP_NABL2, false)).map(NaBL2ConfigReaderWriter::read)
                .orElse(NaBL2Config.DEFAULT);
    }


    @Override public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);
        Optional.ofNullable(configurationAt(PROP_NABL2, false)).ifPresent(c -> {
            messages.addAll(NaBL2ConfigReaderWriter.validate(c, mb));
        });
        return messages;
    }

}