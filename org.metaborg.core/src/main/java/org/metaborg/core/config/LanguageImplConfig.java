package org.metaborg.core.config;

import java.util.Collection;
import java.util.Set;

import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LanguageImplConfig implements ILanguageImplConfig {
    private final Set<LanguageIdentifier> compileDeps = Sets.newHashSet();
    private final Set<LanguageIdentifier> sourceDeps = Sets.newHashSet();
    private final Set<LanguageIdentifier> javaDeps = Sets.newHashSet();
    private final Collection<IGenerateConfig> generates = Lists.newArrayList();
    private final Collection<IExportConfig> exports = Lists.newArrayList();


    public LanguageImplConfig(Iterable<ILanguageComponentConfig> configs) {
        for(ILanguageComponentConfig config : configs) {
            compileDeps.addAll(config.compileDeps());
            sourceDeps.addAll(config.sourceDeps());
            javaDeps.addAll(config.javaDeps());
            generates.addAll(config.generates());
            exports.addAll(config.exports());
        }
    }


    @Override public Iterable<LanguageIdentifier> compileDeps() {
        return compileDeps;
    }

    @Override public Iterable<LanguageIdentifier> sourceDeps() {
        return sourceDeps;
    }

    @Override public Iterable<LanguageIdentifier> javaDeps() {
        return javaDeps;
    }

    @Override public Iterable<IGenerateConfig> generates() {
        return generates;
    }

    @Override public Iterable<IExportConfig> exports() {
        return exports;
    }
}
