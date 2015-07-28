package org.metaborg.core.test;

import org.junit.Before;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.resource.IResourceService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MetaborgTest {
    private final AbstractModule module;

    protected Injector injector;

    protected IResourceService resourceService;
    protected ILanguageService languageService;
    protected ILanguageDiscoveryService languageDiscoveryService;
    protected ILanguageIdentifierService languageIdentifierService;


    public MetaborgTest(AbstractModule module) {
        this.module = module;
    }


    @Before public void beforeTest() {
        injector = Guice.createInjector(module);

        resourceService = injector.getInstance(IResourceService.class);
        languageService = injector.getInstance(ILanguageService.class);
        languageDiscoveryService = injector.getInstance(ILanguageDiscoveryService.class);
        languageIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
    }
}
