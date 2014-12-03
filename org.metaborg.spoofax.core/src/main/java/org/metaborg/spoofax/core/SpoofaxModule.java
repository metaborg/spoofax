package org.metaborg.spoofax.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageFacetFactory;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.parser.IParseService;
import org.metaborg.spoofax.core.parser.jsglr.JSGLRParseService;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.util.logging.Log4JTypeListener;
import org.spoofax.interpreter.library.IOperatorRegistry;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class SpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        try {
            bindListener(Matchers.any(), new Log4JTypeListener());

            bind(IResourceService.class).to(ResourceService.class).in(Singleton.class);
            bind(ITermFactoryService.class).to(TermFactoryService.class).in(Singleton.class);
            bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
            bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);
            bind(ILanguageIdentifierService.class).to(LanguageIdentifierService.class).in(Singleton.class);
            bind(IParseService.class).to(JSGLRParseService.class).in(Singleton.class);
            bind(IStrategoRuntimeService.class).to(StrategoRuntimeService.class).in(Singleton.class);

            bind(FileSystemManager.class).toProvider(DefaultFileSystemManagerProvider.class).in(
                Singleton.class);

            @SuppressWarnings("unused") final Multibinder<ILanguageFacetFactory> facetFactoriesBinder =
                Multibinder.newSetBinder(binder(), ILanguageFacetFactory.class);
            @SuppressWarnings("unused") final Multibinder<IOperatorRegistry> strategoLibraryBinder =
                Multibinder.newSetBinder(binder(), IOperatorRegistry.class);

            bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(
                this.getClass().getClassLoader());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
