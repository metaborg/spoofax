package org.metaborg.spoofax.meta.core;

import com.google.inject.Singleton;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.project.IDependencyService;
import org.metaborg.spoofax.core.project.IProjectService;

public class SpoofaxMetaModule extends SpoofaxModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    protected void bindDependency() {
        bind(IDependencyService.class).to(MavenDependencyService.class).in(Singleton.class);
    }

    @Override
    protected void bindProject() {
        bind(IProjectService.class).to(DefaultMavenProjectService.class).in(Singleton.class);
        bind(IMavenProjectService.class).to(DefaultMavenProjectService.class).in(Singleton.class);
    }

    @Override
    protected void bindBuilder() {
        super.bindBuilder();
        bind(SpoofaxMetaBuilder.class).in(Singleton.class);
    }

}
