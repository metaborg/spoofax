package org.metaborg.meta.core;

import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.ILanguageSpecConfigWriter;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.LanguageSpecConfigService;
import org.metaborg.meta.core.config.LegacyLanguageSpecConfigService;
import org.metaborg.meta.core.project.ILanguageSpecService;
import org.metaborg.meta.core.project.LegacyLanguageSpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class MetaborgMetaModule extends AbstractModule {
    @Override protected void configure() {
        bindLanguageSpec();
        bindLanguageSpecConfig();
    }

    protected void bindLanguageSpec() {
        bind(ILanguageSpecService.class).to(LegacyLanguageSpecService.class).in(Singleton.class);
    }

    protected void bindLanguageSpecConfig() {
        // FIXME: Used to bridge between the old and the new configuration systems.
        bind(LegacyLanguageSpecConfigService.class).in(Singleton.class);
        bind(ILanguageSpecConfigService.class).to(LegacyLanguageSpecConfigService.class).in(Singleton.class);

        bind(LanguageSpecConfigService.class).in(Singleton.class);
        bind(ILanguageSpecConfigWriter.class).to(LanguageSpecConfigService.class);

        bind(LanguageSpecConfigBuilder.class);
        bind(ILanguageSpecConfigBuilder.class).to(LanguageSpecConfigBuilder.class);
    }
}
