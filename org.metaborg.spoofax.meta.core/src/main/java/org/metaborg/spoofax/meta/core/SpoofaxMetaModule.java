package org.metaborg.spoofax.meta.core;

import org.metaborg.meta.core.MetaborgMetaModule;
import org.metaborg.spoofax.meta.core.ant.AntRunnerService;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class SpoofaxMetaModule extends MetaborgMetaModule {
    @Override protected void configure() {
//        bind(MetaBuildAntRunnerFactory.class).in(Singleton.class);
        bind(SpoofaxMetaBuilder.class).in(Singleton.class);

//        bind(SpoofaxLanguageSpecBuilder.class).in(Singleton.class);
//        bind(NewMetaBuildAntRunnerFactory.class).in(Singleton.class);

        Multibinder.newSetBinder(binder(), IBuildStep.class);

        bindAnt();
    }

    protected void bindAnt() {
        bind(IAntRunnerService.class).to(AntRunnerService.class).in(Singleton.class);
    }
}
