package org.metaborg.spoofax.core;

import org.jukito.UseModules;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;

import com.google.inject.Inject;

@UseModules(SpoofaxModule.class) public class SpoofaxTest {
    @Inject protected IResourceService resourceService;
    @Inject protected ITermFactoryService termFactoryService;
    @Inject protected ILanguageService languageService;
    @Inject protected ILanguageDiscoveryService languageDiscoveryService;
}
