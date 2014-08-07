package org.metaborg.spoofax.core;

import org.metaborg.spoofax.core.esv.ESVLanguageFacetFactory;
import org.metaborg.spoofax.core.language.ILanguageFacetFactory;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class SpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        try {
            bind(IResourceService.class).to(ResourceService.class);
            bind(ILanguageService.class).to(LanguageService.class);
            bind(SpoofaxSession.class).asEagerSingleton();

            final Multibinder<ILanguageFacetFactory> facetFactoriesBinder =
                Multibinder.newSetBinder(binder(), ILanguageFacetFactory.class);
            facetFactoriesBinder.addBinding().to(ESVLanguageFacetFactory.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
