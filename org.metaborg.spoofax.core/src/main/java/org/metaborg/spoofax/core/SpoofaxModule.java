package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.analysis.stratego.StrategoAnalysisService;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.LocalFileProvider;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
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
            bindSyntax();
            bindSourceText();
            bindAnalysis();
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

        final Multibinder<IOperatorRegistry> libraryBinder =
            Multibinder.newSetBinder(binder(), IOperatorRegistry.class);
        bindPrimitiveLibrary(libraryBinder, TaskLibrary.class);
        bindPrimitiveLibrary(libraryBinder, LegacyIndexLibrary.class);
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
