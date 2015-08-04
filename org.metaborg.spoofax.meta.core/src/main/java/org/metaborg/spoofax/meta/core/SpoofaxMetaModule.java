package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.meta.core.ant.AntRunnerService;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class SpoofaxMetaModule extends AbstractModule {
    @Override protected void configure() {
        bind(MetaBuildAntRunnerFactory.class).in(Singleton.class);
        bind(SpoofaxMetaBuilder.class).in(Singleton.class);

        bindAnt();
    }

    protected void bindAnt() {
        bind(IAntRunnerService.class).to(AntRunnerService.class).in(Singleton.class);
    }
}
