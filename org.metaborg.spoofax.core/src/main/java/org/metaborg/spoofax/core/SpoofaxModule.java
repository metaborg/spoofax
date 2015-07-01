package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.analysis.stratego.StrategoAnalysisService;
import org.metaborg.spoofax.core.build.IBuilder;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.build.SpoofaxBuilder;
import org.metaborg.spoofax.core.build.dependency.IDependencyService;
import org.metaborg.spoofax.core.build.dependency.MavenDependencyService;
import org.metaborg.spoofax.core.build.paths.DependencyPathProvider;
import org.metaborg.spoofax.core.build.paths.ILanguagePathProvider;
import org.metaborg.spoofax.core.build.paths.ILanguagePathService;
import org.metaborg.spoofax.core.build.paths.SpoofaxLanguagePathService;
import org.metaborg.spoofax.core.build.paths.SpoofaxProjectPathProvider;
import org.metaborg.spoofax.core.completion.ICompletionService;
import org.metaborg.spoofax.core.completion.jsglr.JSGLRCompletionService;
import org.metaborg.spoofax.core.context.ContextService;
import org.metaborg.spoofax.core.context.IContextFactory;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.context.IContextStrategy;
import org.metaborg.spoofax.core.context.LanguageContextStrategy;
import org.metaborg.spoofax.core.context.ProjectContextStrategy;
import org.metaborg.spoofax.core.context.ResourceContextStrategy;
import org.metaborg.spoofax.core.context.SpoofaxContextFactory;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.language.dialect.DialectService;
import org.metaborg.spoofax.core.language.dialect.IDialectIdentifier;
import org.metaborg.spoofax.core.language.dialect.IDialectProcessor;
import org.metaborg.spoofax.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.language.dialect.StrategoDialectIdentifier;
import org.metaborg.spoofax.core.language.dialect.StrategoDialectProcessor;
import org.metaborg.spoofax.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.spoofax.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultRequester;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.analyze.SpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.parse.IParseResultProcessor;
import org.metaborg.spoofax.core.processing.parse.IParseResultRequester;
import org.metaborg.spoofax.core.processing.parse.IParseResultUpdater;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultRequester;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultUpdater;
import org.metaborg.spoofax.core.processing.parse.SpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.project.DummyProjectService;
import org.metaborg.spoofax.core.project.IMavenProjectService;
import org.metaborg.spoofax.core.project.IProjectService;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.source.ISourceTextService;
import org.metaborg.spoofax.core.source.SourceTextService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.primitives.DummyPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ForeignCallPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageIncludesPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageSourcesPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ParseFilePrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ParseFilePtPrimitive;
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
import org.metaborg.spoofax.core.syntax.jsglr.JSGLRSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.spoofax.core.tracing.IHoverService;
import org.metaborg.spoofax.core.tracing.IReferenceResolver;
import org.metaborg.spoofax.core.tracing.ITracingService;
import org.metaborg.spoofax.core.tracing.spoofax.ISpoofaxHoverService;
import org.metaborg.spoofax.core.tracing.spoofax.ISpoofaxReferenceResolver;
import org.metaborg.spoofax.core.tracing.spoofax.ISpoofaxTracingService;
import org.metaborg.spoofax.core.tracing.spoofax.SpoofaxReferences;
import org.metaborg.spoofax.core.tracing.spoofax.SpoofaxTracingService;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.ITransformerExecutor;
import org.metaborg.spoofax.core.transform.ITransformerGoal;
import org.metaborg.spoofax.core.transform.ITransformerResultHandler;
import org.metaborg.spoofax.core.transform.NamedGoal;
import org.metaborg.spoofax.core.transform.stratego.IStrategoTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoCompileTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoNamedTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformer;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformerCommon;
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

    protected Multibinder<ILanguageCache> languageCacheBinder;


    public SpoofaxModule() {
        this(SpoofaxModule.class.getClassLoader());
    }

    public SpoofaxModule(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }


    @Override protected void configure() {
        languageCacheBinder = Multibinder.newSetBinder(binder(), ILanguageCache.class);

        bindResource();
        bindLanguage();
        bindLanguagePath();
        bindLanguagePathProviders(Multibinder.newSetBinder(binder(), ILanguagePathProvider.class));
        bindContext();
        bindContextStrategies(MapBinder.newMapBinder(binder(), String.class, IContextStrategy.class));
        bindProject();
        bindDependency();
        bindSyntax();
        bindCompletion();
        bindSourceText();
        bindAnalysis();
        bindTransformer();
        bindTransformerResultHandlers(MapBinder.newMapBinder(binder(),
            new TypeLiteral<Class<? extends ITransformerGoal>>() {},
            new TypeLiteral<ITransformerResultHandler<IStrategoTerm>>() {}));
        bindBuilder();
        bindCategorizer();
        bindStyler();
        bindTracing();
        bindOther();

        bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(resourceClassLoader);
    }

    protected void bindResource() {
        bind(IResourceService.class).to(ResourceService.class).in(Singleton.class);
        bind(FileSystemManager.class).toProvider(DefaultFileSystemManagerProvider.class).in(Singleton.class);
    }

    protected void bindLanguage() {
        bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
        bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);
        bind(ILanguageIdentifierService.class).to(LanguageIdentifierService.class).in(Singleton.class);

        bind(IDialectService.class).to(DialectService.class).in(Singleton.class);
        bind(IDialectIdentifier.class).to(StrategoDialectIdentifier.class).in(Singleton.class);
        bind(IDialectProcessor.class).to(StrategoDialectProcessor.class).in(Singleton.class);
    }

    protected void bindLanguagePath() {
        bind(ILanguagePathService.class).to(SpoofaxLanguagePathService.class).in(Singleton.class);
    }

    protected void bindLanguagePathProviders(Multibinder<ILanguagePathProvider> binder) {
        binder.addBinding().to(SpoofaxProjectPathProvider.class);
        binder.addBinding().to(DependencyPathProvider.class);
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
        bind(DummyProjectService.class).in(Singleton.class);
        bind(IProjectService.class).to(DummyProjectService.class);
        bind(IMavenProjectService.class).to(DummyProjectService.class);
    }

    protected void bindDependency() {
        bind(IDependencyService.class).to(MavenDependencyService.class).in(Singleton.class);
    }

    protected void bindSyntax() {
        bind(JSGLRSyntaxService.class).in(Singleton.class);
        bind(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}).to(JSGLRSyntaxService.class);
        bind(new TypeLiteral<ISyntaxService<?>>() {}).to(JSGLRSyntaxService.class);
        languageCacheBinder.addBinding().to(JSGLRSyntaxService.class);
        bind(ITermFactoryService.class).to(TermFactoryService.class).in(Singleton.class);
    }

    protected void bindCompletion() {
        bind(ICompletionService.class).to(JSGLRCompletionService.class).in(Singleton.class);
    }

    protected void bindSourceText() {
        bind(ISourceTextService.class).to(SourceTextService.class).in(Singleton.class);
    }

    protected void bindAnalysis() {
        bind(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}).to(StrategoAnalysisService.class)
            .in(Singleton.class);
        bind(StrategoRuntimeService.class).in(Singleton.class);
        bind(IStrategoRuntimeService.class).to(StrategoRuntimeService.class);
        languageCacheBinder.addBinding().to(StrategoRuntimeService.class);
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
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageSourcesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageIncludesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, ForeignCallPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_set_total_work_units", 0, 0));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_set_markers", 0, 1));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_refreshresource", 0, 1));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_queue_strategy", 0, 2));
        bindPrimitive(spoofaxPrimitiveLibrary, new DummyPrimitive("SSL_EXT_complete_work_unit", 0, 0));

        final Multibinder<AbstractPrimitive> spoofaxJSGLRLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named("SpoofaxJSGLRLibrary"));
        bindPrimitive(spoofaxJSGLRLibrary, ParseFilePrimitive.class);
        bindPrimitive(spoofaxJSGLRLibrary, ParseFilePtPrimitive.class);
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

        bind(StrategoTransformer.class).in(Singleton.class);
        bind(IStrategoTransformer.class).to(StrategoTransformer.class);
        bind(new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(
            StrategoTransformer.class);
        bind(new TypeLiteral<ITransformer<?, ?, ?>>() {}).to(StrategoTransformer.class);
    }

    protected void bindTransformerResultHandlers(
        MapBinder<Class<? extends ITransformerGoal>, ITransformerResultHandler<IStrategoTerm>> binder) {
        bind(StrategoTransformerFileWriter.class).in(Singleton.class);
        binder.addBinding(NamedGoal.class).to(StrategoTransformerFileWriter.class);
        binder.addBinding(CompileGoal.class).to(StrategoTransformerFileWriter.class);
    }

    protected void bindBuilder() {
        bind(SpoofaxParseResultProcessor.class).in(Singleton.class);

        bind(ISpoofaxParseResultRequester.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultRequester<IStrategoTerm>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultRequester<?>>() {}).to(SpoofaxParseResultProcessor.class);

        bind(ISpoofaxParseResultUpdater.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultUpdater<IStrategoTerm>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultUpdater<?>>() {}).to(SpoofaxParseResultProcessor.class);

        bind(ISpoofaxParseResultProcessor.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultProcessor<IStrategoTerm>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultProcessor<?>>() {}).to(SpoofaxParseResultProcessor.class);


        bind(SpoofaxAnalysisResultProcessor.class).in(Singleton.class);

        bind(ISpoofaxAnalysisResultRequester.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultRequester<IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultRequester<?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);

        bind(ISpoofaxAnalysisResultUpdater.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultUpdater<IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultUpdater<?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);

        bind(ISpoofaxAnalysisResultProcessor.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultProcessor<IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultProcessor<?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);


        bind(SpoofaxBuilder.class).in(Singleton.class);
        bind(ISpoofaxBuilder.class).to(SpoofaxBuilder.class);
        bind(IBuilder.class).to(SpoofaxBuilder.class);
        bind(new TypeLiteral<IBuilder<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(SpoofaxBuilder.class);
        bind(new TypeLiteral<IBuilder<?, ?, ?>>() {}).to(SpoofaxBuilder.class);
    }

    protected void bindCategorizer() {
        bind(new TypeLiteral<ICategorizerService<IStrategoTerm, IStrategoTerm>>() {}).to(CategorizerService.class).in(
            Singleton.class);
    }

    protected void bindStyler() {
        bind(new TypeLiteral<IStylerService<IStrategoTerm, IStrategoTerm>>() {}).to(StylerService.class).in(
            Singleton.class);
    }

    protected void bindTracing() {
        bind(SpoofaxTracingService.class).in(Singleton.class);
        bind(ISpoofaxTracingService.class).to(SpoofaxTracingService.class);
        bind(new TypeLiteral<ITracingService<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxTracingService.class);

        bind(SpoofaxReferences.class).in(Singleton.class);
        bind(ISpoofaxReferenceResolver.class).to(SpoofaxReferences.class);
        bind(new TypeLiteral<IReferenceResolver<IStrategoTerm, IStrategoTerm>>() {}).to(SpoofaxReferences.class);
        bind(ISpoofaxHoverService.class).to(SpoofaxReferences.class);
        bind(new TypeLiteral<IHoverService<IStrategoTerm, IStrategoTerm>>() {}).to(SpoofaxReferences.class);
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
