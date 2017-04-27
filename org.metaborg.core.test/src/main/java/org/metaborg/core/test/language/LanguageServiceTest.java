package org.metaborg.core.test.language;

import static org.junit.Assert.*;
import static org.metaborg.util.test.Assert2.*;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.metaborg.core.MetaborgModule;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.DescriptionFacet;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.test.MetaborgTest;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;

import com.google.common.collect.Iterables;

public class LanguageServiceTest extends MetaborgTest {
    public LanguageServiceTest(MetaborgModule module) {
        super(module);
    }


    /**
     * Add multiple different language implementations, assert correctness of the language service.
     */
    @Test public void languageServiceCorrectness() throws Exception {
        final String id1 = "org.metaborg.lang.entity1";
        final String id2 = "org.metaborg.lang.entity2";
        final String id3 = "org.metaborg.lang.entity3";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final FileObject location3 = createDir("ram:///Entity3");
        final String name1 = "Entity1";
        final String name2 = "Entity2";
        final String name3 = "Entity3";

        final ILanguageComponent component1 = language(identifier1, location1, name1);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang1 = impl1.belongsTo();
        final ILanguageComponent component2 = language(identifier2, location2, name2);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);
        final ILanguage lang2 = impl2.belongsTo();
        final ILanguageComponent component3 = language(identifier3, location3, name3);
        final ILanguageImpl impl3 = Iterables.get(component3.contributesTo(), 0);
        final ILanguage lang3 = impl3.belongsTo();

        assertEquals(component1, languageService.getComponent(location1.getName()));
        assertSame(component1, languageService.getComponent(location1.getName()));
        assertEquals(component2, languageService.getComponent(location2.getName()));
        assertSame(component2, languageService.getComponent(location2.getName()));
        assertEquals(component3, languageService.getComponent(location3.getName()));
        assertSame(component3, languageService.getComponent(location3.getName()));
        assertEquals(impl1, languageService.getImpl(identifier1));
        assertSame(impl1, languageService.getImpl(identifier1));
        assertEquals(impl2, languageService.getImpl(identifier2));
        assertSame(impl2, languageService.getImpl(identifier2));
        assertEquals(impl3, languageService.getImpl(identifier3));
        assertSame(impl3, languageService.getImpl(identifier3));
        assertEquals(impl1, lang1.activeImpl());
        assertSame(impl1, lang1.activeImpl());
        assertEquals(impl2, lang2.activeImpl());
        assertSame(impl2, lang2.activeImpl());
        assertEquals(impl3, lang3.activeImpl());
        assertSame(impl3, lang3.activeImpl());
        assertEquals(lang1, languageService.getLanguage(name1));
        assertSame(lang1, languageService.getLanguage(name1));
        assertEquals(lang2, languageService.getLanguage(name2));
        assertSame(lang2, languageService.getLanguage(name2));
        assertEquals(lang3, languageService.getLanguage(name3));
        assertSame(lang3, languageService.getLanguage(name3));
        assertSize(1, impl1.components());
        assertSize(1, impl2.components());
        assertSize(1, impl3.components());
        assertSize(1, lang1.impls());
        assertSize(1, lang2.impls());
        assertSize(1, lang3.impls());
        assertSize(3, languageService.getAllComponents());
        assertSize(3, languageService.getAllImpls());
        assertSize(3, languageService.getAllLanguages());
    }

    /**
     * Add an implementation with a higher version number, assert that the newer implementation becomes active.
     */
    @Test public void activeHigherVersion() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 0, 1);
        final LanguageVersion version2 = version(0, 1, 0);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id, version1);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id, version2);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();

        assertSame(component1, languageService.getComponent(location1.getName()));
        assertSame(impl1, languageService.getImpl(identifier1));
        assertSame(impl1, lang.activeImpl());
        assertSame(lang, languageService.getLanguage(name));

        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);

        // Language 2 with higher version number becomes active.
        assertSame(component1, languageService.getComponent(location1.getName()));
        assertSame(component2, languageService.getComponent(location2.getName()));
        assertSame(impl1, languageService.getImpl(identifier1));
        assertSame(impl2, languageService.getImpl(identifier2));
        assertSame(impl2, lang.activeImpl());
        assertSame(lang, languageService.getLanguage(name));
        assertSize(1, impl1.components());
        assertSize(1, impl2.components());
        assertSize(2, lang.impls());
        assertSize(2, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());
    }

    /**
     * Add an implementation with a lower version number, assert that the other implementation stays active.
     */
    @Test public void activeLowerVersion() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 1, 0);
        final LanguageVersion version2 = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id, version1);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id, version2);
        final FileObject location1 = createDir("ram:///Entity1/");
        final FileObject location2 = createDir("ram:///Entity2/");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();

        assertSame(component1, languageService.getComponent(location1.getName()));
        assertSame(impl1, languageService.getImpl(identifier1));
        assertSame(impl1, lang.activeImpl());
        assertSame(lang, languageService.getLanguage(name));

        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);

        // Language 1 with higher version number stays active.
        assertSame(component1, languageService.getComponent(location1.getName()));
        assertSame(component2, languageService.getComponent(location2.getName()));
        assertSame(impl1, languageService.getImpl(identifier1));
        assertSame(impl2, languageService.getImpl(identifier2));
        assertSame(impl1, lang.activeImpl());
        assertSame(lang, languageService.getLanguage(name));
        assertSize(1, impl1.components());
        assertSize(1, impl2.components());
        assertSize(2, lang.impls());
        assertSize(2, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());
    }

    /**
     * Add multiple implementations, assert that the active language is correct.
     */
    @Test public void activeMostRecent() throws Exception {
        final String id1 = "org.metaborg.lang.entity1";
        final String id2 = "org.metaborg.lang.entity2";
        final String id3 = "org.metaborg.lang.entity3";
        final String id4 = "org.metaborg.lang.entity4";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final LanguageIdentifier identifier4 = new LanguageIdentifier(groupId, id4, version);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final FileObject location3 = createDir("ram:///Entity3");
        final FileObject location4 = createDir("ram:///Entity3");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();
        assertSame(impl1, lang.activeImpl());
        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);
        assertSame(impl2, lang.activeImpl());
        final ILanguageComponent component3 = language(identifier3, location3, name);
        final ILanguageImpl impl3 = Iterables.get(component3.contributesTo(), 0);
        assertSame(impl3, lang.activeImpl());

        languageService.remove(component3);
        assertSame(impl2, lang.activeImpl());

        languageService.remove(component1);
        assertSame(impl2, lang.activeImpl());

        final ILanguageComponent component4 = language(identifier4, location4, name);
        final ILanguageImpl impl4 = Iterables.get(component4.contributesTo(), 0);
        assertSame(impl4, lang.activeImpl());

        languageService.remove(component4);
        assertSame(impl2, lang.activeImpl());

        languageService.remove(component2);
        assertNull(lang.activeImpl());
        assertSize(0, lang.impls());
        assertNull(languageService.getLanguage(name));
    }

    /**
     * Reload a single component, assert that it was reloaded and its implementations and language stays the same.
     */
    @Test public void reloadComponent() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDir("ram:///");
        final String name = "Entity";

        // GTODO: test multiple contributing components
        final ILanguageComponent componentBefore = language(groupId, id, version, location, name);
        final ILanguageImpl implBefore = Iterables.get(componentBefore.contributesTo(), 0);
        final ILanguage langBefore = implBefore.belongsTo();

        assertSame(componentBefore, languageService.getComponent(location.getName()));

        final ILanguageComponent componentAfter = language(groupId, id, version, location, name);
        final ILanguageImpl implAfter = Iterables.get(componentAfter.contributesTo(), 0);
        final ILanguage langAfter = implAfter.belongsTo();

        // Before components are equal, but not the same object, since they are re-created.
        assertEquals(componentBefore, languageService.getComponent(location.getName()));
        assertNotSame(componentBefore, languageService.getComponent(location.getName()));
        assertEquals(componentBefore, componentAfter);
        assertNotSame(componentBefore, componentAfter);

        assertSame(componentAfter, languageService.getComponent(location.getName()));

        assertSame(implBefore, implAfter);
        assertSame(langBefore, langAfter);
    }

    /**
     * Add multiple components that contribute to multiple implementations, assert correctness of language service and
     * objects. Delete some components and assert correctness again.
     */
    @Test public void componentContributions() throws Exception {
        final String id1 = "org.metaborg.lang.entity.component1";
        final String id2 = "org.metaborg.lang.entity.component2";
        final String id3 = "org.metaborg.lang.entity.component3";
        final String id4 = "org.metaborg.lang.entity.component4";
        final String id5 = "org.metaborg.lang.entity.component5";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final LanguageIdentifier identifier4 = new LanguageIdentifier(groupId, id4, version);
        final LanguageIdentifier identifier5 = new LanguageIdentifier(groupId, id5, version);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final FileObject location3 = createDir("ram:///Entity3");
        final FileObject location4 = createDir("ram:///Entity4");
        final FileObject location5 = createDir("ram:///Entity5");
        final String implId1 = "org.metaborg.lang.entity.impl1";
        final String implId2 = "org.metaborg.lang.entity.impl2";
        final String name = "Entity";
        final LanguageIdentifier implIdentifier1 = new LanguageIdentifier(groupId, implId1, version);
        final LanguageIdentifier implIdentifier2 = new LanguageIdentifier(groupId, implId2, version);
        final LanguageContributionIdentifier requestIdentifier1 =
                new LanguageContributionIdentifier(implIdentifier1, name);
        final LanguageContributionIdentifier requestIdentifier2 =
                new LanguageContributionIdentifier(implIdentifier2, name);

        final ILanguageComponent component1 = language(identifier1, location1, requestIdentifier1);
        final ILanguageComponent component2 = language(identifier2, location2, requestIdentifier1);
        final ILanguageComponent component3 = language(identifier3, location3, requestIdentifier2);
        final ILanguageComponent component4 = language(identifier4, location4, requestIdentifier1, requestIdentifier2);
        final ILanguageComponent component5 = language(identifier5, location5, requestIdentifier1, requestIdentifier2);
        final ILanguageImpl impl1 = languageService.getImpl(implIdentifier1);
        final ILanguageImpl impl2 = languageService.getImpl(implIdentifier2);
        final ILanguage lang = languageService.getLanguage(name);

        assertSame(component1, languageService.getComponent(location1.getName()));
        assertSame(component2, languageService.getComponent(location2.getName()));
        assertSame(component3, languageService.getComponent(location3.getName()));
        assertSame(component4, languageService.getComponent(location4.getName()));
        assertSame(component5, languageService.getComponent(location5.getName()));
        assertSame(impl1, languageService.getImpl(implIdentifier1));
        assertSame(impl2, languageService.getImpl(implIdentifier2));
        assertSame(lang, languageService.getLanguage(name));
        assertSize(5, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());

        assertContains(impl1, component1.contributesTo());
        assertNotContains(impl2, component1.contributesTo());
        assertContains(impl1, component2.contributesTo());
        assertNotContains(impl2, component2.contributesTo());
        assertNotContains(impl1, component3.contributesTo());
        assertContains(impl2, component3.contributesTo());
        assertContains(impl1, component4.contributesTo());
        assertContains(impl2, component4.contributesTo());
        assertContains(impl1, component5.contributesTo());
        assertContains(impl2, component5.contributesTo());

        assertContains(component1, impl1.components());
        assertContains(component2, impl1.components());
        assertNotContains(component3, impl1.components());
        assertContains(component4, impl1.components());
        assertContains(component5, impl1.components());

        assertNotContains(component1, impl2.components());
        assertNotContains(component2, impl2.components());
        assertContains(component3, impl2.components());
        assertContains(component4, impl2.components());
        assertContains(component5, impl2.components());

        assertSame(lang, impl1.belongsTo());
        assertSame(lang, impl2.belongsTo());

        assertContains(impl1, lang.impls());
        assertContains(impl2, lang.impls());

        languageService.remove(component3);

        assertNull(languageService.getComponent(location3.getName()));
        assertSize(4, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());

        assertEmpty(component3.contributesTo());
        assertNotContains(component3, impl1.components());

        languageService.remove(component4);
        languageService.remove(component5);

        // Also removes implementation 2, since all its components have been removed.
        assertNull(languageService.getImpl(implIdentifier2));
        assertSize(2, languageService.getAllComponents());
        assertSize(1, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());

        assertEmpty(component4.contributesTo());
        assertEmpty(component5.contributesTo());

        assertNotContains(component4, impl1.components());
        assertNotContains(component5, impl1.components());

        assertEmpty(impl2.components());

        assertNotContains(impl2, lang.impls());
    }

    /**
     * Add contributions iwth conflicting language names, assert that this throws an error.
     */
    @Test(expected = IllegalStateException.class) public void conflictingContributionNames() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final String id1 = "org.metaborg.lang.entity.component1";
        final String id2 = "org.metaborg.lang.entity.component2";

        final LanguageVersion version = version(0, 0, 1);

        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);

        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");

        final String name1 = "Entity1";
        final String name2 = "Entity2";

        language(identifier1, location1, new LanguageContributionIdentifier(identifier, name1));
        language(identifier2, location2, new LanguageContributionIdentifier(identifier, name2));
    }

    /**
     * Add multiple components with facets to a single implementation, assert correctness of returned facets.
     */
    @Test public void implementationFacets() throws Exception {
        final String id1 = "org.metaborg.lang.entity.component1";
        final String id2 = "org.metaborg.lang.entity.component2";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final String implId = "org.metaborg.lang.entity.impl1";
        final String name = "Entity";
        final LanguageIdentifier implIdentifier = new LanguageIdentifier(groupId, implId, version);
        final LanguageContributionIdentifier requestIdentifier =
                new LanguageContributionIdentifier(implIdentifier, name);

        final DescriptionFacet facet1 = new DescriptionFacet("Component1", null);
        final ResourceExtensionFacet facet2 = new ResourceExtensionFacet(Iterables2.singleton("com"));
        final DescriptionFacet facet3 = new DescriptionFacet("Component2", null);

        final ILanguageComponent component1 = language(identifier1, location1, requestIdentifier, facet1, facet2);
        final ILanguageComponent component2 = language(identifier2, location2, requestIdentifier, facet3);
        final ILanguageImpl impl = languageService.getImpl(implIdentifier);

        assertContains(facet1, component1.facets());
        assertContains(facet2, component1.facets());
        assertContains(facet3, component2.facets());
        assertSize(2, component1.facets());
        assertSize(1, component2.facets());

        assertContains(facet1, component1.facets(DescriptionFacet.class));
        assertSame(facet1, component1.facet(DescriptionFacet.class));
        assertSize(1, component1.facets(DescriptionFacet.class));
        assertContains(facet2, component1.facets(ResourceExtensionFacet.class));
        assertSame(facet2, component1.facet(ResourceExtensionFacet.class));
        assertSize(1, component1.facets(ResourceExtensionFacet.class));
        assertContains(facet3, component2.facets(DescriptionFacet.class));
        assertSame(facet3, component2.facet(DescriptionFacet.class));
        assertSize(1, component2.facets(DescriptionFacet.class));

        assertContains(facet1, impl.facets());
        assertContains(facet2, impl.facets());
        assertContains(facet3, impl.facets());
        assertSize(3, impl.facets());

        assertContains(facet1, impl.facets(DescriptionFacet.class));
        assertContains(facet2, impl.facets(ResourceExtensionFacet.class));
        assertContains(facet3, impl.facets(DescriptionFacet.class));
        assertSize(2, impl.facets(DescriptionFacet.class));
        assertSize(1, impl.facets(ResourceExtensionFacet.class));

        for(FacetContribution<IFacet> facetContribution : component1.facetContributions()) {
            assertSame(facetContribution.contributor, component1);
        }
        assertSame(component1, component1.facetContribution(DescriptionFacet.class).contributor);
        assertSame(facet1, component1.facetContribution(DescriptionFacet.class).facet);
        assertSize(1, component1.facetContributions(DescriptionFacet.class));
        assertSame(component1, component1.facetContribution(ResourceExtensionFacet.class).contributor);
        assertSame(facet2, component1.facetContribution(ResourceExtensionFacet.class).facet);
        assertSize(1, component1.facetContributions(ResourceExtensionFacet.class));

        for(FacetContribution<IFacet> facetContribution : component2.facetContributions()) {
            assertSame(facetContribution.contributor, component2);
        }
        assertSame(component2, component2.facetContribution(DescriptionFacet.class).contributor);
        assertSame(facet3, component2.facetContribution(DescriptionFacet.class).facet);
        assertSize(1, component2.facetContributions(DescriptionFacet.class));

        for(FacetContribution<IFacet> facetContribution : impl.facetContributions()) {
            final ILanguageComponent contributor = facetContribution.contributor;
            final IFacet facet = facetContribution.facet;
            if(facet.equals(facet1)) {
                assertSame(component1, contributor);
            } else if(facet.equals(facet2)) {
                assertSame(component1, contributor);
            } else if(facet.equals(facet3)) {
                assertSame(component2, contributor);
            } else {
                fail("Facet does not equals any created facet");
            }
        }
    }

    /**
     * Add/reload/remove components, assert change events correctness.
     */
    @Test public void languageChanges() throws Exception {
        final String id1 = "org.metaborg.lang.entity.component1";
        final String id2 = "org.metaborg.lang.entity.component2";
        final String id3 = "org.metaborg.lang.entity.component3";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");
        final FileObject location3 = createDir("ram:///Entity3");
        final String implId1 = "org.metaborg.lang.entity.impl1";
        final String implId2 = "org.metaborg.lang.entity.impl2";
        final String implId3 = "org.metaborg.lang.entity.impl3";
        final String name = "Entity";
        final LanguageIdentifier implIdentifier1 = new LanguageIdentifier(groupId, implId1, version);
        final LanguageIdentifier implIdentifier2 = new LanguageIdentifier(groupId, implId2, version);
        final LanguageIdentifier implIdentifier3 = new LanguageIdentifier(groupId, implId3, version);
        final LanguageContributionIdentifier requestIdentifier1 =
                new LanguageContributionIdentifier(implIdentifier1, name);
        final LanguageContributionIdentifier requestIdentifier2 =
                new LanguageContributionIdentifier(implIdentifier2, name);
        final LanguageContributionIdentifier requestIdentifier3 =
                new LanguageContributionIdentifier(implIdentifier3, name);

        final ITestableObserver<LanguageComponentChange> compObs = new TestableObserver<LanguageComponentChange>();
        languageService.componentChanges().subscribe(compObs);
        final ITestableObserver<LanguageImplChange> implObs = new TestableObserver<LanguageImplChange>();
        languageService.implChanges().subscribe(implObs);


        // Add component1 to impl1, expect component add, impl add
        final ILanguageComponent component1 = language(identifier1, location1, requestIdentifier1);
        final ILanguageImpl impl1 = languageService.getImpl(implIdentifier1);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Add, null, component1), compObs);
        assertOnNext(new LanguageImplChange(LanguageImplChange.Kind.Add, impl1), implObs);

        // Add component2 to impl1, expect component2 add, impl1 reload
        final ILanguageComponent component2 = language(identifier2, location2, requestIdentifier1);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Add, null, component2), compObs);
        assertOnNext(new LanguageImplChange(LanguageImplChange.Kind.Reload, impl1), implObs);

        // Add component3 to impl1, impl2, impl3, expect component add, [impl1 reload, impl2 add, impl3 add] (order
        // unknown)
        final ILanguageComponent component3 =
                language(identifier3, location3, requestIdentifier1, requestIdentifier2, requestIdentifier3);
        final ILanguageImpl impl2 = languageService.getImpl(implIdentifier2);
        final ILanguageImpl impl3 = languageService.getImpl(implIdentifier3);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Add, null, component3), compObs);
        {
            final Iterable<LanguageImplChange> changes =
                    Iterables2.from(new LanguageImplChange(LanguageImplChange.Kind.Reload, impl1),
                            new LanguageImplChange(LanguageImplChange.Kind.Add, impl2),
                            new LanguageImplChange(LanguageImplChange.Kind.Add, impl3));
            assertOnNext(changes, implObs);
            assertOnNext(changes, implObs);
            assertOnNext(changes, implObs);
        }

        // Remove component1, expect component1 remove, impl1 reload
        languageService.remove(component1);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Remove, component1, null), compObs);
        assertOnNext(new LanguageImplChange(LanguageImplChange.Kind.Reload, impl1), implObs);

        // Reload component2, contribute to impl2 now, expect component2 reload, [impl1 reload, impl2 reload] (order
        // unknown)
        final ILanguageComponent component2Reload = language(identifier2, location2, requestIdentifier2);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Reload, component2, component2Reload),
                compObs);
        {
            final Iterable<LanguageImplChange> changes =
                    Iterables2.from(new LanguageImplChange(LanguageImplChange.Kind.Reload, impl1),
                            new LanguageImplChange(LanguageImplChange.Kind.Reload, impl2));
            assertOnNext(changes, implObs);
            assertOnNext(changes, implObs);
        }

        // Remove component3, expect component3 remove, [impl1 remove, impl2 reload, impl3 remove] (order unknown)
        languageService.remove(component3);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Remove, component3, null), compObs);
        {
            final Iterable<LanguageImplChange> changes =
                    Iterables2.from(new LanguageImplChange(LanguageImplChange.Kind.Remove, impl3),
                            new LanguageImplChange(LanguageImplChange.Kind.Reload, impl2),
                            new LanguageImplChange(LanguageImplChange.Kind.Remove, impl1));
            assertOnNext(changes, implObs);
            assertOnNext(changes, implObs);
            assertOnNext(changes, implObs);
        }

        // Remove component2, expect component2 remove, impl2 remove
        languageService.remove(component2);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Remove, component2, null), compObs);
        assertOnNext(new LanguageImplChange(LanguageImplChange.Kind.Remove, impl2), implObs);

        assertEmpty(compObs);
        assertEmpty(implObs);
    }

    /**
     * Try to get a single facet, but have multiple. Assert that exception is thrown.
     */
    @Test(expected = MetaborgRuntimeException.class) public void multipleUnexpectedFacets() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDir("ram:///");

        final ILanguageComponent component = language(groupId, "org.metaborg.lang.entity", version, location, "Entity",
                new DescriptionFacet("Entity language", null), new DescriptionFacet("Entity language", null));
        component.facet(DescriptionFacet.class);
    }

    /**
     * Try to add component with non-existent location. Assert that exception is thrown.
     */
    @Test(expected = IllegalStateException.class) public void nonExistentLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = resourceService.resolve("ram:///doesnotexist");

        language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
    }

    /**
     * Try to remove component that was not added to the language service. Assert that exception is thrown.
     */
    @Test(expected = IllegalStateException.class) public void nonExistentLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDir("ram:///");

        final ILanguageComponent component = language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
        languageService.remove(component);
        languageService.remove(component);
    }

}