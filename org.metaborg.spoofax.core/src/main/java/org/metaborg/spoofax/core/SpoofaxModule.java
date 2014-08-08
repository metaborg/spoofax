package org.metaborg.spoofax.core;

import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageFacetFactory;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.util.logging.Log4JTypeListener;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class SpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        try {
            bindListener(Matchers.any(), new Log4JTypeListener());

            bind(IResourceService.class).to(ResourceService.class);
            bind(ITermFactoryService.class).to(TermFactoryService.class);
            bind(ILanguageService.class).to(LanguageService.class);
            bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class);

            final Multibinder<ILanguageFacetFactory> facetFactoriesBinder =
                Multibinder.newSetBinder(binder(), ILanguageFacetFactory.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
