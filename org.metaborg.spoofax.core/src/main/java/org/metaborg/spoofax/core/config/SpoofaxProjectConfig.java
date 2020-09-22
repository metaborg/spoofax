package org.metaborg.spoofax.core.config;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.ProjectConfig;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.spoofax.core.build.SpoofaxDefaultSources;
import org.metaborg.spoofax.core.config.language.NaBL2ConfigReaderWriter;

import mb.nabl2.config.NaBL2Config;

public class SpoofaxProjectConfig extends ProjectConfig implements ISpoofaxProjectConfig {

    private static final String PROP_RUNTIME = "runtime";

    private static final String PROP_NABL2 = PROP_RUNTIME + ".nabl2";

    private static final String PROP_LANGUAGE = "language";

    private static final String PROP_STATIX = PROP_LANGUAGE + ".statix";
    private static final String PROP_STATIX_CONCURRENT = PROP_STATIX + ".concurrent";

    public SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected SpoofaxProjectConfig(HierarchicalConfiguration<ImmutableNode> config, String metaborgVersion,
            Collection<IExportConfig> sources, Collection<LanguageIdentifier> compileDeps,
            Collection<LanguageIdentifier> sourceDeps, Collection<LanguageIdentifier> javaDeps,
            NaBL2Config nabl2Config, @Nullable Boolean statixConcurrent) {
        super(config, metaborgVersion, sources, compileDeps, sourceDeps, javaDeps);
        if(nabl2Config != null) {
            Optional.ofNullable(configurationAt(PROP_NABL2, true))
                    .ifPresent(c -> NaBL2ConfigReaderWriter.write(nabl2Config, c));
        }
        if(statixConcurrent != null) {
            config.setProperty(PROP_STATIX_CONCURRENT, statixConcurrent);
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

    @Override public NaBL2Config nabl2Config() {
        return Optional.ofNullable(configurationAt(PROP_NABL2, false)).map(NaBL2ConfigReaderWriter::read)
                .orElse(NaBL2Config.DEFAULT);
    }

    @Override public boolean statixConcurrent() {
        return config.getBoolean(PROP_STATIX_CONCURRENT, false);
    }

    @Override public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);
        Optional.ofNullable(configurationAt(PROP_NABL2, false)).ifPresent(c -> {
            messages.addAll(NaBL2ConfigReaderWriter.validate(c, mb));
        });
        return messages;
    }

}