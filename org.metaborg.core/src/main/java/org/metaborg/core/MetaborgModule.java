package org.metaborg.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.core.build.Builder;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.build.paths.DependencyPathProvider;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.build.processing.analyze.AnalysisResultProcessor;
import org.metaborg.core.build.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.build.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.build.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.build.processing.parse.IParseResultProcessor;
import org.metaborg.core.build.processing.parse.IParseResultRequester;
import org.metaborg.core.build.processing.parse.IParseResultUpdater;
import org.metaborg.core.build.processing.parse.ParseResultProcessor;
import org.metaborg.core.context.ContextService;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.context.LanguageContextStrategy;
import org.metaborg.core.context.ProjectContextStrategy;
import org.metaborg.core.context.ResourceContextStrategy;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifierService;
import org.metaborg.core.language.LanguageService;
import org.metaborg.core.project.DummyProjectService;
import org.metaborg.core.project.IMavenProjectService;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.source.SourceTextService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class MetaborgModule extends AbstractModule {
    private final ClassLoader resourceClassLoader;

    protected Multibinder<ILanguageCache> languageCacheBinder;


    public MetaborgModule() {
        this(MetaborgModule.class.getClassLoader());
    }

    public MetaborgModule(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }


    @Override protected void configure() {
        languageCacheBinder = Multibinder.newSetBinder(binder(), ILanguageCache.class);

        bindResource();
        bindLanguage();
        bindLanguagePathProviders(Multibinder.newSetBinder(binder(), ILanguagePathProvider.class));
        bindContext();
        bindContextStrategies(MapBinder.newMapBinder(binder(), String.class, IContextStrategy.class));
        bindProject();
        bindSourceText();
        bindBuilder();

        bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(resourceClassLoader);
    }


    protected void bindResource() {
        bind(IResourceService.class).to(ResourceService.class).in(Singleton.class);
        bind(FileSystemManager.class).toProvider(DefaultFileSystemManagerProvider.class).in(Singleton.class);
    }

    protected void bindLanguage() {
        bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
        bind(ILanguageIdentifierService.class).to(LanguageIdentifierService.class).in(Singleton.class);
    }

    protected void bindLanguagePathProviders(Multibinder<ILanguagePathProvider> binder) {
        binder.addBinding().to(DependencyPathProvider.class);
    }

    protected void bindContext() {
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

    protected void bindSourceText() {
        bind(ISourceTextService.class).to(SourceTextService.class).in(Singleton.class);
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
    }
}
