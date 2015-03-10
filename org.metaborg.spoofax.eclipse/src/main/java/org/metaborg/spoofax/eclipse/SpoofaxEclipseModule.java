package org.metaborg.spoofax.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.project.IProjectService;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.eclipse.editor.LatestEditorListener;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.processing.Processor;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;
import org.metaborg.spoofax.eclipse.resource.EclipseLocalFileProvider;
import org.metaborg.spoofax.eclipse.resource.EclipseProjectService;
import org.metaborg.spoofax.eclipse.resource.EclipseResourceService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

public class SpoofaxEclipseModule extends SpoofaxModule {
    public SpoofaxEclipseModule() {
        super(SpoofaxEclipseModule.class.getClassLoader());
    }

    public SpoofaxEclipseModule(ClassLoader resourceClassLoader) {
        super(resourceClassLoader);
    }


    @Override protected void bindResource() {
        bind(EclipseResourceService.class).in(Singleton.class);
        bind(IResourceService.class).to(EclipseResourceService.class);
        bind(IEclipseResourceService.class).to(EclipseResourceService.class);

        bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(Singleton.class);
    }

    protected void bindLocalFileProviders(MapBinder<String, ILocalFileProvider> binder) {
        super.bindLocalFileProviders(binder);

        binder.addBinding(EclipseLocalFileProvider.scheme).to(EclipseLocalFileProvider.class).in(Singleton.class);
    }

    @Override protected void bindProject() {
        bind(IProjectService.class).to(EclipseProjectService.class).in(Singleton.class);
    }

    @Override protected void bindOther() {
        bind(GlobalSchedulingRules.class).asEagerSingleton();
        bind(Processor.class).asEagerSingleton();
        bind(ParseResultProcessor.class).asEagerSingleton();
        bind(AnalysisResultProcessor.class).asEagerSingleton();
        bind(LatestEditorListener.class).asEagerSingleton();
    }
}
