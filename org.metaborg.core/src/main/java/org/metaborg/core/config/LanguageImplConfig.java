package org.metaborg.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.metaborg.core.language.LanguageIdentifier;

public class LanguageImplConfig implements ILanguageImplConfig {
    private final Set<LanguageIdentifier> compileDeps = new HashSet<LanguageIdentifier>();
    private final Set<LanguageIdentifier> sourceDeps = new HashSet<LanguageIdentifier>();
    private final Set<LanguageIdentifier> javaDeps = new HashSet<LanguageIdentifier>();
    private final Collection<IGenerateConfig> generates = new ArrayList<>();
    private final Collection<IExportConfig> exports = new ArrayList<>();


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
