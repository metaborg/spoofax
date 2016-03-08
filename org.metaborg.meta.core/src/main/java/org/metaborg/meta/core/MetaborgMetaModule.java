package org.metaborg.meta.core;

import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.ILanguageSpecConfigWriter;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.LanguageSpecConfigService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class MetaborgMetaModule extends AbstractModule {
    protected Multibinder<AutoCloseable> autoClosableBinder;


    @Override protected void configure() {
        autoClosableBinder = Multibinder.newSetBinder(binder(), AutoCloseable.class, Meta.class);

        bindLanguageSpec();
        bindLanguageSpecConfig();
    }

    protected void bindLanguageSpec() {
    }

    protected void bindLanguageSpecConfig() {
        bind(LanguageSpecConfigService.class).in(Singleton.class);
        bind(ILanguageSpecConfigWriter.class).to(LanguageSpecConfigService.class);
        bind(ILanguageSpecConfigService.class).to(LanguageSpecConfigService.class);

        bind(LanguageSpecConfigBuilder.class);
        bind(ILanguageSpecConfigBuilder.class).to(LanguageSpecConfigBuilder.class);
    }
}
