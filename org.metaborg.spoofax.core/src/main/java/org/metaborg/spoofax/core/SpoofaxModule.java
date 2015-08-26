package org.metaborg.spoofax.core;

import org.metaborg.core.MetaborgModule;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.build.paths.LanguagePathService;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.processing.IProcessor;
import org.metaborg.core.processing.IProcessorRunner;
import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.core.processing.parse.IParseResultRequester;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.core.style.ICategorizerService;
import org.metaborg.core.style.IStylerService;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.core.tracing.IReferenceResolver;
import org.metaborg.core.tracing.ITracingService;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.core.transform.ITransformer;
import org.metaborg.core.transform.ITransformerExecutor;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.ITransformerResultHandler;
import org.metaborg.core.transform.NamedGoal;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisService;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.build.SpoofaxBuilder;
import org.metaborg.spoofax.core.build.paths.SpoofaxLanguagePathProvider;
import org.metaborg.spoofax.core.completion.JSGLRCompletionService;
import org.metaborg.spoofax.core.context.SpoofaxContextFactory;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.dialect.DialectService;
import org.metaborg.spoofax.core.language.dialect.StrategoDialectIdentifier;
import org.metaborg.spoofax.core.language.dialect.StrategoDialectProcessor;
import org.metaborg.spoofax.core.menu.MenuService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessor;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.processing.SpoofaxBlockingProcessor;
import org.metaborg.spoofax.core.processing.SpoofaxProcessorRunner;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultRequester;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.analyze.SpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultRequester;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultUpdater;
import org.metaborg.spoofax.core.processing.parse.SpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.project.DummyMavenProjectService;
import org.metaborg.spoofax.core.project.IMavenProjectService;
import org.metaborg.spoofax.core.project.settings.ISpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.ProjectSettingsService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettingsService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.primitives.DummyPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ForeignCallPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageIncludeFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageIncludeLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageSourceFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LanguageSourceLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LegacyLanguageIncludeLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.LegacyLanguageSourceLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ParseFilePrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ParseFilePtPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.ProjectPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitives.SpoofaxJSGLRLibrary;
import org.metaborg.spoofax.core.stratego.primitives.SpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.core.stratego.strategies.ParseFileStrategy;
import org.metaborg.spoofax.core.stratego.strategies.ParseStrategoFileStrategy;
import org.metaborg.spoofax.core.style.CategorizerService;
import org.metaborg.spoofax.core.style.StylerService;
import org.metaborg.spoofax.core.syntax.JSGLRSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.spoofax.core.terms.TermPrettyPrinter;
import org.metaborg.spoofax.core.tracing.ISpoofaxHoverService;
import org.metaborg.spoofax.core.tracing.ISpoofaxReferenceResolver;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.tracing.SpoofaxReferences;
import org.metaborg.spoofax.core.tracing.SpoofaxTracingService;
import org.metaborg.spoofax.core.transform.IStrategoTransformer;
import org.metaborg.spoofax.core.transform.StrategoCompileTransformer;
import org.metaborg.spoofax.core.transform.StrategoNamedTransformer;
import org.metaborg.spoofax.core.transform.StrategoTransformer;
import org.metaborg.spoofax.core.transform.StrategoTransformerCommon;
import org.metaborg.spoofax.core.transform.StrategoTransformerFileWriter;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.index.primitives.legacy.LegacyIndexLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class SpoofaxModule extends MetaborgModule {
    public SpoofaxModule() {
        this(SpoofaxModule.class.getClassLoader());
    }

    public SpoofaxModule(ClassLoader resourceClassLoader) {
        super(resourceClassLoader);
    }


    @Override protected void configure() {
        super.configure();

        bindLanguagePath();
        bindMavenProject();
        bindSyntax();
        bindCompletion();
        bindAnalysis();
        bindTransformer();
        bindTransformerResultHandlers(MapBinder.newMapBinder(binder(),
            new TypeLiteral<Class<? extends ITransformerGoal>>() {},
            new TypeLiteral<ITransformerResultHandler<IStrategoTerm>>() {}));
        bindCategorizer();
        bindStyler();
        bindTracing();
        bindMenu();
    }


    @Override protected void bindLanguage() {
        super.bindLanguage();

        bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);

        bind(IDialectService.class).to(DialectService.class).in(Singleton.class);
        bind(IDialectIdentifier.class).to(StrategoDialectIdentifier.class).in(Singleton.class);
        bind(IDialectProcessor.class).to(StrategoDialectProcessor.class).in(Singleton.class);
    }

    protected void bindLanguagePath() {
        bind(ILanguagePathService.class).to(LanguagePathService.class).in(Singleton.class);
    }

    @Override protected void bindLanguagePathProviders(Multibinder<ILanguagePathProvider> binder) {
        super.bindLanguagePathProviders(binder);

        binder.addBinding().to(SpoofaxLanguagePathProvider.class);
    }

    @Override protected void bindContext() {
        super.bindContext();

        bind(IContextFactory.class).to(SpoofaxContextFactory.class).in(Singleton.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProjectSettings()} for non-dummy implementation of project settings service.
     */
    @Override protected void bindProjectSettings() {
        bind(IProjectSettingsService.class).to(ProjectSettingsService.class).in(Singleton.class);
        bind(ISpoofaxProjectSettingsService.class).to(SpoofaxProjectSettingsService.class).in(Singleton.class);
    }

    protected void bindMavenProject() {
        bind(IMavenProjectService.class).to(DummyMavenProjectService.class).in(Singleton.class);
    }

    protected void bindSyntax() {
        bind(JSGLRSyntaxService.class).in(Singleton.class);
        bind(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}).to(JSGLRSyntaxService.class);
        bind(new TypeLiteral<ISyntaxService<?>>() {}).to(JSGLRSyntaxService.class);
        languageCacheBinder.addBinding().to(JSGLRSyntaxService.class);
        bind(ITermFactoryService.class).to(TermFactoryService.class).in(Singleton.class);
        bind(TermPrettyPrinter.class).in(Singleton.class);
    }

    protected void bindCompletion() {
        bind(ICompletionService.class).to(JSGLRCompletionService.class).in(Singleton.class);
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
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageSourceLocationsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LegacyLanguageSourceLocationsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageSourceFilesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageIncludeLocationsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LegacyLanguageIncludeLocationsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageIncludeFilesPrimitive.class);
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

    /**
     * Overrides {@link MetaborgModule#bindBuilder()} to provide Spoofax-specific bindings with generics filled in as
     * {@link IStrategoTerm}.
     */
    @Override protected void bindBuilder() {
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

    /**
     * Overrides {@link MetaborgModule#bindProcessorRunner()} to provide Spoofax-specific bindings with generics filled
     * in as {@link IStrategoTerm}.
     */
    @Override protected void bindProcessorRunner() {
        bind(SpoofaxProcessorRunner.class).in(Singleton.class);
        bind(ISpoofaxProcessorRunner.class).to(SpoofaxProcessorRunner.class);
        bind(IProcessorRunner.class).to(SpoofaxProcessorRunner.class);
        bind(new TypeLiteral<IProcessorRunner<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxProcessorRunner.class);
        bind(new TypeLiteral<IProcessorRunner<?, ?, ?>>() {}).to(SpoofaxProcessorRunner.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProcessor()} to provide Spoofax-specific bindings with generics filled in as
     * {@link IStrategoTerm}.
     */
    @Override protected void bindProcessor() {
        bind(SpoofaxBlockingProcessor.class).in(Singleton.class);
        bind(ISpoofaxProcessor.class).to(SpoofaxBlockingProcessor.class);
        bind(IProcessor.class).to(SpoofaxBlockingProcessor.class);
        bind(new TypeLiteral<IProcessor<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}).to(
            SpoofaxBlockingProcessor.class);
        bind(new TypeLiteral<IProcessor<?, ?, ?>>() {}).to(SpoofaxBlockingProcessor.class);
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

    protected void bindMenu() {
        bind(IMenuService.class).to(MenuService.class).in(Singleton.class);
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
