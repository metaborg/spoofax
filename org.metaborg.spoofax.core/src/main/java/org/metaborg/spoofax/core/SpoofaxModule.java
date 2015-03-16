package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.analysis.stratego.StrategoAnalysisService;
import org.metaborg.spoofax.core.context.ContextService;
import org.metaborg.spoofax.core.context.IContextFactory;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.context.IContextStrategy;
import org.metaborg.spoofax.core.context.LanguageContextStrategy;
import org.metaborg.spoofax.core.context.ProjectContextStrategy;
import org.metaborg.spoofax.core.context.ResourceContextStrategy;
import org.metaborg.spoofax.core.context.SpoofaxContextFactory;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.project.DummyProjectService;
import org.metaborg.spoofax.core.project.IProjectService;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.LocalFileProvider;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.primitives.DummyPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ParseFilePrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ProjectPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.SpoofaxJSGLRLibrary;
import org.metaborg.spoofax.core.stratego.primitives.SpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.core.stratego.strategies.ParseFileStrategy;
import org.metaborg.spoofax.core.stratego.strategies.ParseStrategoFileStrategy;
import org.metaborg.spoofax.core.style.CategorizerService;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.style.StylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.jsglr.JSGLRParseService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.spoofax.core.text.SourceTextService;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.ITransformerExecutor;
import org.metaborg.spoofax.core.transform.ITransformerGoal;
import org.metaborg.spoofax.core.transform.ITransformerResultHandler;
import org.metaborg.spoofax.core.transform.NamedGoal;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformerCommon;
import org.metaborg.spoofax.core.transform.stratego.StrategoCompileTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoNamedTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformerFileWriter;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.index.legacy.LegacyIndexLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class SpoofaxModule extends AbstractModule {
    private final ClassLoader resourceClassLoader;


    public SpoofaxModule() {
        this(SpoofaxModule.class.getClassLoader());
    }

    public SpoofaxModule(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }


    @Override protected void configure() {
        try {
            bindResource();
            bindLocalFileProviders(MapBinder.newMapBinder(binder(), String.class, ILocalFileProvider.class));
            bindLanguage();
            bindContext();
            bindContextStrategies(MapBinder.newMapBinder(binder(), String.class, IContextStrategy.class));
            bindProject();
            bindSyntax();
            bindSourceText();
            bindAnalysis();
            bindTransformer();
            bindTransformerResultHandlers(MapBinder.newMapBinder(binder(),
                new TypeLiteral<Class<? extends ITransformerGoal>>() {},
                new TypeLiteral<ITransformerResultHandler<IStrategoTerm>>() {}));
            bindCategorizer();
            bindStyler();
            bindOther();

            bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(resourceClassLoader);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void bindResource() {
        bind(IResourceService.class).to(ResourceService.class).in(Singleton.class);
        bind(FileSystemManager.class).toProvider(DefaultFileSystemManagerProvider.class).in(Singleton.class);
    }

    protected void bindLocalFileProviders(MapBinder<String, ILocalFileProvider> binder) {
        binder.addBinding(LocalFileProvider.scheme).to(LocalFileProvider.class).in(Singleton.class);
    }

    protected void bindLanguage() {
        bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
        bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);
        bind(ILanguageIdentifierService.class).to(LanguageIdentifierService.class).in(Singleton.class);
    }

    protected void bindContext() {
        bind(IContextFactory.class).to(SpoofaxContextFactory.class).in(Singleton.class);
        bind(IContextService.class).to(ContextService.class).in(Singleton.class);
    }

    protected void bindContextStrategies(MapBinder<String, IContextStrategy> binder) {
        binder.addBinding(ResourceContextStrategy.name).to(ResourceContextStrategy.class).in(Singleton.class);
        binder.addBinding(LanguageContextStrategy.name).to(LanguageContextStrategy.class).in(Singleton.class);
        binder.addBinding(ProjectContextStrategy.name).to(ProjectContextStrategy.class).in(Singleton.class);
    }

    protected void bindProject() {
        bind(IProjectService.class).to(DummyProjectService.class).in(Singleton.class);
    }

    protected void bindSyntax() {
        bind(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}).to(JSGLRParseService.class).in(Singleton.class);
        bind(ITermFactoryService.class).to(TermFactoryService.class).in(Singleton.class);
    }

    protected void bindSourceText() {
        bind(ISourceTextService.class).to(SourceTextService.class).in(Singleton.class);
    }

    protected void bindAnalysis() {
        bind(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}).to(StrategoAnalysisService.class)
            .in(Singleton.class);
        bind(IStrategoRuntimeService.class).to(StrategoRuntimeService.class).in(Singleton.class);
        bind(StrategoLocalPath.class).in(Singleton.class);

        bind(ParseFileStrategy.class).in(Singleton.class);
        bind(ParseStrategoFileStrategy.class).in(Singleton.class);

        final Multibinder<IOperatorRegistry> libraryBinder =
            Multibinder.newSetBinder(binder(), IOperatorRegistry.class);
        bindPrimitiveLibrary(libraryBinder, TaskLibrary.class);
        bindPrimitiveLibrary(libraryBinder, LegacyIndexLibrary.class);
        bindPrimitiveLibrary(libraryBinder, SpoofaxPrimitiveLibrary.class);
        bindPrimitiveLibrary(libraryBinder, SpoofaxJSGLRLibrary.class);

        final Multibinder<AbstractPrimitive> spoofaxPrimitiveLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named("SpoofaxPrimitiveLibrary"));
        bindPrimitive(spoofaxPrimitiveLibrary, ProjectPathPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_set_total_work_units", 0, 0));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_set_markers", 0, 1));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_refreshresource", 0, 1));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_queue_strategy", 0, 2));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_complete_work_unit", 0, 0));

        final Multibinder<AbstractPrimitive> spoofaxJSGLRLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named("SpoofaxJSGLRLibrary"));
        bindPrimitive(spoofaxJSGLRLibrary, ParseFilePrimitive.class);
        bindPrimitive(spoofaxJSGLRLibrary, new DummyPrimitive("STRSGLR_open_parse_table", 0, 1));
        bindPrimitive(spoofaxJSGLRLibrary, new DummyPrimitive("STRSGLR_close_parse_table", 0, 1));
    }

    protected void bindTransformer() {
        final MapBinder<Class<? extends ITransformerGoal>, ITransformerExecutor<IStrategoTerm, IStrategoTerm, IStrategoTerm>> executorBinder =
            MapBinder.newMapBinder(binder(), new TypeLiteral<Class<? extends ITransformerGoal>>() {},
                new TypeLiteral<ITransformerExecutor<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {});
        executorBinder.addBinding(NamedGoal.class).to(StrategoNamedTransformer.class).in(Singleton.class);
        executorBinder.addBinding(CompileGoal.class).to(StrategoCompileTransformer.class).in(Singleton.class);

        bind(StrategoTransformerCommon.class).in(Singleton.class);
        bind(new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(
            StrategoTransformer.class).in(Singleton.class);
    }

    protected void bindTransformerResultHandlers(
        MapBinder<Class<? extends ITransformerGoal>, ITransformerResultHandler<IStrategoTerm>> binder) {
        bind(StrategoTransformerFileWriter.class).in(Singleton.class);
        binder.addBinding(NamedGoal.class).to(StrategoTransformerFileWriter.class);
        binder.addBinding(CompileGoal.class).to(StrategoTransformerFileWriter.class);
    }

    protected void bindCategorizer() {
        bind(new TypeLiteral<ICategorizerService<IStrategoTerm, IStrategoTerm>>() {}).to(CategorizerService.class).in(
            Singleton.class);
    }

    protected void bindStyler() {
        bind(new TypeLiteral<IStylerService<IStrategoTerm, IStrategoTerm>>() {}).to(StylerService.class).in(
            Singleton.class);
    }


    protected void bindOther() {

    }


    protected static void bindPrimitive(Multibinder<AbstractPrimitive> binder, AbstractPrimitive primitive) {
        binder.addBinding().toInstance(primitive);
    }

    protected static void bindPrimitive(Multibinder<AbstractPrimitive> binder,
        Class<? extends AbstractPrimitive> primitive) {
        binder.addBinding().to(primitive).in(Singleton.class);
    }

    protected static void bindPrimitiveLibrary(Multibinder<IOperatorRegistry> binder,
        Class<? extends IOperatorRegistry> primitiveLibrary) {
        binder.addBinding().to(primitiveLibrary).in(Singleton.class);
    }
}
