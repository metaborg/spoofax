package org.metaborg.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.core.analysis.AnalysisService;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.BuildOutput;
import org.metaborg.core.build.Builder;
import org.metaborg.core.build.IBuildOutputInternal;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.build.dependency.DefaultDependencyService;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.DependencyPathProvider;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.build.paths.LanguagePathService;
import org.metaborg.core.build.paths.SourcePathProvider;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.ILanguageComponentConfigBuilder;
import org.metaborg.core.config.ILanguageComponentConfigService;
import org.metaborg.core.config.ILanguageComponentConfigWriter;
import org.metaborg.core.config.IProjectConfigBuilder;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.config.IProjectConfigWriter;
import org.metaborg.core.config.LanguageComponentConfigBuilder;
import org.metaborg.core.config.LanguageComponentConfigService;
import org.metaborg.core.config.ProjectConfigBuilder;
import org.metaborg.core.config.ProjectConfigService;
import org.metaborg.core.config.YamlConfigurationReaderWriter;
import org.metaborg.core.context.ContextService;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextProcessor;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.context.ProjectContextStrategy;
import org.metaborg.core.context.ResourceContextStrategy;
import org.metaborg.core.editor.DummyEditorRegistry;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifierService;
import org.metaborg.core.language.LanguageService;
import org.metaborg.core.processing.BlockingProcessor;
import org.metaborg.core.processing.ILanguageChangeProcessor;
import org.metaborg.core.processing.IProcessor;
import org.metaborg.core.processing.IProcessorRunner;
import org.metaborg.core.processing.LanguageChangeProcessor;
import org.metaborg.core.processing.ProcessorRunner;
import org.metaborg.core.processing.analyze.AnalysisResultProcessor;
import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.core.processing.parse.IParseResultRequester;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.processing.parse.ParseResultProcessor;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.source.SourceTextService;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.testing.ITestReporterService;
import org.metaborg.core.testing.LoggingTestReporterService;
import org.metaborg.core.testing.TeamCityLogger;
import org.metaborg.core.testing.TeamCityWriter;
import org.metaborg.core.transform.ITransformUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class MetaborgModule extends AbstractModule {
    private final ClassLoader resourceClassLoader;

    protected Multibinder<AutoCloseable> autoClosableBinder;
    protected Multibinder<ILanguageCache> languageCacheBinder;
    protected MapBinder<String, IContextFactory> contextFactoryBinder;
    protected MapBinder<String, IContextStrategy> contextStrategyBinder;
    protected Multibinder<ILanguagePathProvider> languagePathProviderBinder;


    public MetaborgModule() {
        this(MetaborgModule.class.getClassLoader());
    }

    public MetaborgModule(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }


    @Override protected void configure() {
        autoClosableBinder = Multibinder.newSetBinder(binder(), AutoCloseable.class);
        languageCacheBinder = Multibinder.newSetBinder(binder(), ILanguageCache.class);

        contextFactoryBinder = MapBinder.newMapBinder(binder(), String.class, IContextFactory.class);
        contextStrategyBinder = MapBinder.newMapBinder(binder(), String.class, IContextStrategy.class);
        languagePathProviderBinder = Multibinder.newSetBinder(binder(), ILanguagePathProvider.class);

        bindResource();
        bindLanguage();
        bindContext();
        bindContextFactories(contextFactoryBinder);
        bindContextStrategies(contextStrategyBinder);
        bindProject();
        bindConfigMisc();
        bindProjectConfig();
        bindLanguageComponentConfig();
        bindLanguagePath();
        bindLanguagePathProviders(languagePathProviderBinder);
        bindDependency();
        bindSourceText();
        bindAnalysis();
        bindBuilder();
        bindProcessor();
        bindProcessorRunner();
        bindLanguageChangeProcessing();
        bindEditor();
        bindTestFramework();
        bindTestReporter();

        bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(resourceClassLoader);
    }

    protected void bindResource() {
        bind(ResourceService.class).in(Singleton.class);
        bind(IResourceService.class).to(ResourceService.class);
        autoClosableBinder.addBinding().to(ResourceService.class);

        bind(FileSystemManager.class).toProvider(DefaultFileSystemManagerProvider.class).in(Singleton.class);
    }

    protected void bindLanguage() {
        bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
        bind(ILanguageIdentifierService.class).to(LanguageIdentifierService.class).in(Singleton.class);
    }

    protected void bindProject() {
        bind(SimpleProjectService.class).in(Singleton.class);
        bind(ISimpleProjectService.class).to(SimpleProjectService.class);
        bind(IProjectService.class).to(SimpleProjectService.class);
    }

    protected void bindContext() {
        bind(ContextService.class).in(Singleton.class);
        bind(IContextService.class).to(ContextService.class);
        bind(IContextProcessor.class).to(ContextService.class);
    }

    protected void bindContextFactories(@SuppressWarnings("unused") MapBinder<String, IContextFactory> binder) {

    }

    protected void bindContextStrategies(MapBinder<String, IContextStrategy> binder) {
        binder.addBinding(ResourceContextStrategy.name).to(ResourceContextStrategy.class).in(Singleton.class);
        binder.addBinding(ProjectContextStrategy.name).to(ProjectContextStrategy.class).in(Singleton.class);
    }

    protected void bindConfigMisc() {
        bind(AConfigurationReaderWriter.class).to(YamlConfigurationReaderWriter.class).in(Singleton.class);
    }

    protected void bindProjectConfig() {
        bind(ProjectConfigService.class).in(Singleton.class);
        bind(IProjectConfigService.class).to(ProjectConfigService.class);

        bind(IProjectConfigWriter.class).to(ProjectConfigService.class);

        bind(ProjectConfigBuilder.class);
        bind(IProjectConfigBuilder.class).to(ProjectConfigBuilder.class);
    }

    protected void bindLanguageComponentConfig() {
        bind(LanguageComponentConfigService.class).in(Singleton.class);
        bind(ILanguageComponentConfigWriter.class).to(LanguageComponentConfigService.class);
        bind(ILanguageComponentConfigService.class).to(LanguageComponentConfigService.class);

        bind(LanguageComponentConfigBuilder.class);
        bind(ILanguageComponentConfigBuilder.class).to(LanguageComponentConfigBuilder.class);
    }

    protected void bindLanguagePath() {
        bind(ILanguagePathService.class).to(LanguagePathService.class).in(Singleton.class);
    }

    protected void bindLanguagePathProviders(Multibinder<ILanguagePathProvider> binder) {
        // Bind builtin path provider before other providers such that builtin
        // paths have preference over others.
        binder.addBinding().to(SourcePathProvider.class);
        binder.addBinding().to(DependencyPathProvider.class);
    }

    protected void bindDependency() {
        bind(IDependencyService.class).to(DefaultDependencyService.class).in(Singleton.class);
    }

    protected void bindSourceText() {
        bind(ISourceTextService.class).to(SourceTextService.class).in(Singleton.class);
    }

    protected void bindAnalysis() {
        bind(IAnalysisService.class).to(AnalysisService.class).in(Singleton.class);
    }

    protected void bindBuilder() {
        bind(ParseResultProcessor.class).in(Singleton.class);
        bind(IParseResultRequester.class).to(ParseResultProcessor.class);
        bind(IParseResultUpdater.class).to(ParseResultProcessor.class);
        bind(IParseResultProcessor.class).to(ParseResultProcessor.class);

        bind(AnalysisResultProcessor.class).in(Singleton.class);
        bind(IAnalysisResultRequester.class).to(AnalysisResultProcessor.class);
        bind(IAnalysisResultUpdater.class).to(AnalysisResultProcessor.class);
        bind(IAnalysisResultProcessor.class).to(AnalysisResultProcessor.class);

        bind(IBuilder.class).to(Builder.class).in(Singleton.class);

        // No scope for build output, new instance for every request.
        bind(
            new TypeLiteral<IBuildOutputInternal<IParseUnit, IAnalyzeUnit, IAnalyzeUnitUpdate, ITransformUnit<?>>>() {})
                .to(new TypeLiteral<BuildOutput<IParseUnit, IAnalyzeUnit, IAnalyzeUnitUpdate, ITransformUnit<?>>>() {});
    }

    protected void bindProcessorRunner() {
        bind(IProcessorRunner.class).to(ProcessorRunner.class).in(Singleton.class);
    }

    protected void bindProcessor() {
        bind(IProcessor.class).to(BlockingProcessor.class).in(Singleton.class);
    }

    protected void bindLanguageChangeProcessing() {
        bind(ILanguageChangeProcessor.class).to(LanguageChangeProcessor.class).in(Singleton.class);
    }

    protected void bindEditor() {
        bind(IEditorRegistry.class).to(DummyEditorRegistry.class).in(Singleton.class);
    }

    protected void bindTestFramework() {
        bind(TeamCityWriter.class).in(Singleton.class);
        bind(TeamCityLogger.class).in(Singleton.class);
    }

    protected void bindTestReporter() {
        // IDE's would override this to show test results in their UI instead.
        bind(ITestReporterService.class).to(LoggingTestReporterService.class).in(Singleton.class);
    }
}
