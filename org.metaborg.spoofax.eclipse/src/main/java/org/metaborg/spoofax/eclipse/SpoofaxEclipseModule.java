package org.metaborg.spoofax.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.eclipse.processing.GlobalMutexes;
import org.metaborg.spoofax.eclipse.processing.Processor;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;
import org.metaborg.spoofax.eclipse.resource.EclipseLocalFileProvider;
import org.metaborg.spoofax.eclipse.resource.EclipseResourceService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.stratego.primitives.DummyPrimitive;
import org.metaborg.spoofax.eclipse.stratego.primitives.ProjectPathPrimitive;
import org.metaborg.spoofax.eclipse.stratego.primitives.SpoofaxEclipsePrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

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

        bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(
            Singleton.class);
    }

    protected void bindLocalFileProviders(MapBinder<String, ILocalFileProvider> binder) {
        super.bindLocalFileProviders(binder);

        binder.addBinding(EclipseLocalFileProvider.scheme).to(EclipseLocalFileProvider.class)
            .in(Singleton.class);
    }

    @Override protected void bindOther() {
        bind(GlobalMutexes.class).asEagerSingleton();
        bind(Processor.class).asEagerSingleton();

        // Use analysis-cmd to prevent Stratego analysis to schedule on a background thread.
        bind(String.class).annotatedWith(Names.named("LanguageDiscoveryAnalysisOverride"))
            .toInstance("analysis-cmd");

        bindPrimitives();
    }

    private void bindPrimitives() {
        final Multibinder<AbstractPrimitive> primitiveBinder =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class);
        bindPrimitive(primitiveBinder, ProjectPathPrimitive.class);
        bindPrimitive(primitiveBinder, new DummyPrimitive("SSL_EXT_set_total_work_units", 0, 0));
        bindPrimitive(primitiveBinder, new DummyPrimitive("SSL_EXT_set_markers", 0, 1));
        bindPrimitive(primitiveBinder, new DummyPrimitive("SSL_EXT_refreshresource", 0, 1));
        bindPrimitive(primitiveBinder, new DummyPrimitive("SSL_EXT_queue_strategy", 0, 2));
        bindPrimitive(primitiveBinder, new DummyPrimitive("SSL_EXT_complete_work_unit", 0, 0));

        final Multibinder<IOperatorRegistry> libraryBinder =
            Multibinder.newSetBinder(binder(), IOperatorRegistry.class);
        bindPrimitiveLibrary(libraryBinder, SpoofaxEclipsePrimitiveLibrary.class);
    }
}
