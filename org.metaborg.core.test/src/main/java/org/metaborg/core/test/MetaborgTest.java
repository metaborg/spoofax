package org.metaborg.core.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageCreationRequest;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.iterators.Iterables2;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MetaborgTest {
    private final AbstractModule module;

    protected final String groupId = "org.metaborg";

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


    protected LanguageVersion version(int major, int minor, int patch) {
        return new LanguageVersion(major, minor, patch, "");
    }

    protected FileObject createDir(String uri) throws FileSystemException {
        final FileObject file = resourceService.resolve(uri);
        file.createFolder();
        return file;
    }


    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location,
        Iterable<LanguageContributionIdentifier> implIds, IFacet... facets) {
        final LanguageCreationRequest request = languageService.create(identifier, location, implIds);
        for(IFacet facet : facets) {
            request.addFacet(facet);
        }
        return languageService.add(request);
    }

    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location,
        LanguageContributionIdentifier implId) {
        return language(identifier, location, Iterables2.singleton(implId), new IFacet[0]);
    }

    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location,
                                          LanguageContributionIdentifier implId, IFacet... facets) {
        return language(identifier, location, Iterables2.singleton(implId), facets);
    }

    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location,
        LanguageContributionIdentifier... implIds) {
        return language(identifier, location, Iterables2.from(implIds), new IFacet[0]);
    }


    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name,
        IFacet... facets) {
        return language(identifier, location,
            Iterables2.singleton(new LanguageContributionIdentifier(identifier, name)), facets);
    }

    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name) {
        return language(identifier, location, name, new IFacet[0]);
    }


    protected ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name, IFacet... facets) {
        return language(new LanguageIdentifier(groupId, id, version), location, name, facets);
    }

    protected ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name) {
        return language(groupId, id, version, location, name, new IFacet[0]);
    }


    protected ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name,
        String... extensions) {
        return language(identifier, location, name,
            new IdentificationFacet(new ResourceExtensionsIdentifier(Iterables2.from(extensions))));
    }

    protected ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name, String... extensions) {
        return language(new LanguageIdentifier(groupId, id, version), location, name, extensions);
    }
}
