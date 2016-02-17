package org.metaborg.spoofax.meta.core;

import org.metaborg.meta.core.MetaborgMetaModule;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.LanguageSpecConfigService;
import org.metaborg.meta.core.project.ILanguageSpecPathsService;
import org.metaborg.spoofax.meta.core.ant.AntRunnerService;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigWriter;
import org.metaborg.spoofax.meta.core.config.LegacySpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.config.LegacySpoofaxLanguageSpecConfigWriter;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecPathsService;
import org.metaborg.spoofax.meta.core.project.SpoofaxLanguageSpecPathsService;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class SpoofaxMetaModule extends MetaborgMetaModule {
    @Override protected void configure() {
        bind(SpoofaxMetaBuilder.class).in(Singleton.class);

        Multibinder.newSetBinder(binder(), IBuildStep.class);

        bindAnt();
        bindLanguageSpecPath();
        bindLanguageSpecConfig();
    }

    protected void bindAnt() {
        bind(IAntRunnerService.class).to(AntRunnerService.class).in(Singleton.class);
    }


    protected void bindLanguageSpecPath() {
        bind(SpoofaxLanguageSpecPathsService.class).in(Singleton.class);
        bind(ILanguageSpecPathsService.class).to(SpoofaxLanguageSpecPathsService.class).in(Singleton.class);
        bind(ISpoofaxLanguageSpecPathsService.class).to(SpoofaxLanguageSpecPathsService.class).in(Singleton.class);
    }

    @Override protected void bindLanguageSpecConfig() {
        bind(LanguageSpecConfigService.class).in(Singleton.class);
        bind(SpoofaxLanguageSpecConfigService.class).in(Singleton.class);

        bind(LegacySpoofaxLanguageSpecConfigService.class).in(Singleton.class);
        bind(LegacySpoofaxLanguageSpecConfigWriter.class).in(Singleton.class);
        bind(ILanguageSpecConfigService.class).to(LegacySpoofaxLanguageSpecConfigService.class).in(Singleton.class);
        bind(ISpoofaxLanguageSpecConfigService.class).to(LegacySpoofaxLanguageSpecConfigService.class)
            .in(Singleton.class);
        bind(ISpoofaxLanguageSpecConfigWriter.class).to(SpoofaxLanguageSpecConfigService.class).in(Singleton.class);

        bind(SpoofaxLanguageSpecConfigBuilder.class);
        bind(ILanguageSpecConfigBuilder.class).to(SpoofaxLanguageSpecConfigBuilder.class);
        bind(ISpoofaxLanguageSpecConfigBuilder.class).to(SpoofaxLanguageSpecConfigBuilder.class);
    }
}
