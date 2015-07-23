package org.metaborg.core.test.language;

import static org.junit.Assert.*;
import static org.metaborg.util.test.Assert2.assertEmpty;
import static org.metaborg.util.test.Assert2.assertOnNext;
import static org.metaborg.util.test.Assert2.assertSize;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.DescriptionFacet;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageCreationRequest;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.language.LanguageImplIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.test.MetaborgTest;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;

import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;

public class LanguageServiceTest extends MetaborgTest {
    private final String groupId = "org.metaborg";


    public LanguageServiceTest(AbstractModule module) {
        super(module);
    }


    private LanguageVersion version(int major, int minor, int patch) {
        return new LanguageVersion(major, minor, patch, "");
    }

    private FileObject createDirectory(String uri) throws FileSystemException {
        final FileObject file = resourceService.resolve(uri);
        file.createFolder();
        return file;
    }


    private ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name,
        IFacet... facets) {
        final LanguageCreationRequest request =
            languageService.create(identifier, location,
                Iterables2.singleton(new LanguageImplIdentifier(identifier, name)));
        for(IFacet facet : facets) {
            request.addFacet(facet);
        }
        return languageService.add(request);
    }

    private ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name) {
        return language(identifier, location, name, new IFacet[0]);
    }


    private ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name, IFacet... facets) {
        return language(new LanguageIdentifier(groupId, id, version), location, name, facets);
    }

    private ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name) {
        return language(groupId, id, version, location, name, new IFacet[0]);
    }


    private ILanguageComponent language(LanguageIdentifier identifier, FileObject location, String name,
        String... extensions) {
        return language(identifier, location, name,
            new IdentificationFacet(new ResourceExtensionsIdentifier(Iterables2.from(extensions))));
    }

    private ILanguageComponent language(String groupId, String id, LanguageVersion version, FileObject location,
        String name, String... extensions) {
        return language(new LanguageIdentifier(groupId, id, version), location, name, extensions);
    }



    @Test public void addSingleLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);
        final FileObject location = createDirectory("ram:///");
        final String name = "Entity";

        final ILanguageComponent component = language(identifier, location, name);
        final ILanguageImpl impl = Iterables.get(component.contributesTo(), 0);
        final ILanguage lang = impl.belongsTo();

        assertEquals(component, languageService.getComponent(component.location().getName()));
        assertSame(component, languageService.getComponent(component.location().getName()));
        assertEquals(impl, languageService.getImpl(identifier));
        assertSame(impl, languageService.getImpl(identifier));
        assertEquals(impl, lang.activeImpl());
        assertSame(impl, lang.activeImpl());
        assertEquals(lang, languageService.getLanguage(name));
        assertSame(lang, languageService.getLanguage(name));

        assertSize(1, impl.components());
        assertSize(1, lang.impls());
        assertSize(1, languageService.getAllComponents());
        assertSize(1, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());
    }

    @Test public void addDifferentLanguages() throws Exception {
        final String id1 = "org.metaborg.lang.entity1";
        final String id2 = "org.metaborg.lang.entity2";
        final String id3 = "org.metaborg.lang.entity3";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
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

    @Test public void addHigherVersionLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 0, 1);
        final LanguageVersion version2 = version(0, 1, 0);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id, version1);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id, version2);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();

        assertEquals(component1, languageService.getComponent(location1.getName()));
        assertEquals(impl1, languageService.getImpl(identifier1));
        assertEquals(impl1, lang.activeImpl());
        assertEquals(lang, languageService.getLanguage(name));

        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);

        // Language 2 with higher version number becomes active.
        assertEquals(component1, languageService.getComponent(location1.getName()));
        assertEquals(component2, languageService.getComponent(location2.getName()));
        assertEquals(impl1, languageService.getImpl(identifier1));
        assertEquals(impl2, languageService.getImpl(identifier2));
        assertEquals(impl2, lang.activeImpl());
        assertEquals(lang, languageService.getLanguage(name));
        assertSize(1, impl1.components());
        assertSize(1, impl2.components());
        assertSize(2, lang.impls());
        assertSize(2, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());
    }

    @Test public void addLowerVersionLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 1, 0);
        final LanguageVersion version2 = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id, version1);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id, version2);
        final FileObject location1 = createDirectory("ram:///Entity1/");
        final FileObject location2 = createDirectory("ram:///Entity2/");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();

        assertEquals(component1, languageService.getComponent(location1.getName()));
        assertEquals(impl1, languageService.getImpl(identifier1));
        assertEquals(impl1, lang.activeImpl());
        assertEquals(lang, languageService.getLanguage(name));

        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);

        // Language 1 with higher version number stays active.
        assertEquals(component1, languageService.getComponent(location1.getName()));
        assertEquals(component2, languageService.getComponent(location2.getName()));
        assertEquals(impl1, languageService.getImpl(identifier1));
        assertEquals(impl2, languageService.getImpl(identifier2));
        assertEquals(impl1, lang.activeImpl());
        assertEquals(lang, languageService.getLanguage(name));
        assertSize(1, impl1.components());
        assertSize(1, impl2.components());
        assertSize(2, lang.impls());
        assertSize(2, languageService.getAllComponents());
        assertSize(2, languageService.getAllImpls());
        assertSize(1, languageService.getAllLanguages());
    }

    @Test public void mostRecentLanguageActive() throws Exception {
        final String id1 = "org.metaborg.lang.entity1";
        final String id2 = "org.metaborg.lang.entity2";
        final String id3 = "org.metaborg.lang.entity3";
        final String id4 = "org.metaborg.lang.entity4";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier1 = new LanguageIdentifier(groupId, id1, version);
        final LanguageIdentifier identifier2 = new LanguageIdentifier(groupId, id2, version);
        final LanguageIdentifier identifier3 = new LanguageIdentifier(groupId, id3, version);
        final LanguageIdentifier identifier4 = new LanguageIdentifier(groupId, id4, version);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final FileObject location4 = createDirectory("ram:///Entity3");
        final String name = "Entity";

        final ILanguageComponent component1 = language(identifier1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguage lang = impl1.belongsTo();
        assertEquals(impl1, lang.activeImpl());
        final ILanguageComponent component2 = language(identifier2, location2, name);
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);
        assertEquals(impl2, lang.activeImpl());
        final ILanguageComponent component3 = language(identifier3, location3, name);
        final ILanguageImpl impl3 = Iterables.get(component3.contributesTo(), 0);
        assertEquals(impl3, lang.activeImpl());

        languageService.remove(component3);
        assertEquals(impl2, lang.activeImpl());

        languageService.remove(component1);
        assertEquals(impl2, lang.activeImpl());

        final ILanguageComponent component4 = language(identifier4, location4, name);
        final ILanguageImpl impl4 = Iterables.get(component4.contributesTo(), 0);
        assertEquals(impl4, lang.activeImpl());

        languageService.remove(component4);
        assertEquals(impl2, lang.activeImpl());

        languageService.remove(component2);
        assertNull(lang.activeImpl());
        assertSize(0, lang.impls());
        assertNull(languageService.getLanguage(name));
    }

    @Test public void reloadLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");
        final String name = "Entity";

        final ILanguageComponent componentBefore = language(groupId, id, version, location, name);
        final ILanguageImpl implBefore = Iterables.get(componentBefore.contributesTo(), 0);
        final ILanguage langBefore = implBefore.belongsTo();

        assertEquals(componentBefore, languageService.getComponent(location.getName()));

        final ILanguageComponent componentAfter = language(groupId, id, version, location, name);
        final ILanguageImpl implAfter = Iterables.get(componentAfter.contributesTo(), 0);
        final ILanguage langAfter = implAfter.belongsTo();

        assertNotEquals(componentBefore, languageService.getComponent(location.getName()));
        assertEquals(componentAfter, languageService.getComponent(location.getName()));
        assertNotSame(componentBefore, componentAfter);
        assertNotEquals(componentBefore, componentAfter);
        assertSame(implBefore, implAfter);
        assertEquals(implBefore, implAfter);
        assertSame(langBefore, langAfter);
        assertEquals(langBefore, langAfter);
    }

    @Test public void identification() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        final ILanguageComponent component1 =
            language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent1");
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        final ILanguageComponent component2 =
            language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent2");
        final ILanguageImpl impl2 = Iterables.get(component2.contributesTo(), 0);

        assertTrue(languageIdentifierService.identify(resourceService.resolve("ram:///Entity1/test.ent1"), impl1));
        assertFalse(languageIdentifierService.identify(resourceService.resolve("ram:///Entity2/test.ent2"), impl1));
        assertTrue(languageIdentifierService.identify(resourceService.resolve("ram:///Entity2/test.ent2"), impl2));
        assertFalse(languageIdentifierService.identify(resourceService.resolve("ram:///Entity1/test.ent1"), impl1));
    }

    @Test public void observables() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 0, 1);
        final LanguageVersion version2 = version(0, 0, 2);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final String name = "Entity";

        final ITestableObserver<LanguageComponentChange> compObs = new TestableObserver<LanguageComponentChange>();
        languageService.componentChanges().subscribe(compObs);
        final ITestableObserver<LanguageImplChange> implObs = new TestableObserver<LanguageImplChange>();
        languageService.implChanges().subscribe(implObs);


        // Add language, expect component add, then implementation add
        final ILanguageComponent component1 = language(groupId, id, version1, location1, name);
        final ILanguageImpl impl1 = Iterables.get(component1.contributesTo(), 0);
        assertOnNext(new LanguageComponentChange(LanguageComponentChange.Kind.Add, null, component1), compObs);
        assertOnNext(new LanguageImplChange(LanguageImplChange.Kind.Add, impl1), implObs);


        // final TimestampedNotification<LanguageImplChange> language1Load = implObs.poll();
        // final TimestampedNotification<LanguageImplChange> language1Add = implObs.poll();
        // assertTrue(language1Load.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.ADD_FIRST, null, language1),
        // language1Load.notification.getValue());
        // assertTrue(language1Add.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.ADD, null, language1),
        // language1Add.notification.getValue());
        //
        //
        // // Create and remove facet, expect ADD and REMOVE.
        // language1.facetChanges().subscribe(facetObserver);
        // final IFacet facet = language1.addFacet(new DescriptionFacet("Entity language", null));
        // final TimestampedNotification<LanguageFacetChange> addedFacet = facetObserver.poll();
        // assertTrue(addedFacet.notification.isOnNext());
        // assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADD),
        // addedFacet.notification.getValue());
        //
        // language1.removeFacet(DescriptionFacet.class);
        // final TimestampedNotification<LanguageFacetChange> removed = facetObserver.poll();
        // assertTrue(removed.notification.isOnNext());
        // assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.REMOVE),
        // removed.notification.getValue());
        // assertEquals(facetObserver.size(), 0);
        //
        //
        // // Add language2 with same name and version, but different location. Expect ADD and REPLACE_ACTIVE.
        // final ILanguageComponent component2 = language(groupId, id, version1, location2, name);
        // final TimestampedNotification<LanguageImplChange> language2Add = implObs.poll();
        // final TimestampedNotification<LanguageImplChange> language2Replace = implObs.poll();
        // assertTrue(language2Add.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.ADD, null, language2),
        // language2Add.notification.getValue());
        // assertTrue(language2Replace.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REPLACE_ACTIVE, language1, language2),
        // language2Replace.notification.getValue());
        //
        //
        // // Add language2 again, expect RELOAD_ACTIVE.
        // final ILanguageComponent component2Again = language(groupId, id, version1, location2, name);
        // final TimestampedNotification<LanguageImplChange> language2ReloadActive = implObs.poll();
        // assertEquals(language2, language2Again);
        // assertNotSame(language2, language2Again);
        // assertTrue(language2ReloadActive.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.RELOAD_ACTIVE, language2, language2Again),
        // language2ReloadActive.notification.getValue());
        // assertSame(language2, language2ReloadActive.notification.getValue().oldLanguage);
        // assertSame(language2Again, language2ReloadActive.notification.getValue().newLanguage);
        //
        //
        // // Add language3 with same name, but higher version and different location. Expect ADD and REPLACE_ACTIVE.
        // final ILanguageComponent component3 = language(groupId, id, version2, location3, name);
        // final TimestampedNotification<LanguageImplChange> language3Add = implObs.poll();
        // final TimestampedNotification<LanguageImplChange> language3Replace = implObs.poll();
        // assertTrue(language3Add.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.ADD, null, language3),
        // language3Add.notification.getValue());
        // assertTrue(language3Replace.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REPLACE_ACTIVE, language2, language3),
        // language3Replace.notification.getValue());
        //
        //
        // // Add language2 again, expect RELOAD.
        // final ILanguageComponent component2AgainAgain = language(groupId, id, version1, location2, name);
        // final TimestampedNotification<LanguageImplChange> language2Reload = implObs.poll();
        // assertEquals(language2Again, language2AgainAgain);
        // assertNotSame(language2Again, language2AgainAgain);
        // assertNotSame(language2, language2AgainAgain);
        // assertTrue(language2Reload.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.RELOAD, language2Again, language2AgainAgain),
        // language2Reload.notification.getValue());
        // assertSame(language2Again, language2Reload.notification.getValue().oldLanguage);
        // assertSame(language2AgainAgain, language2Reload.notification.getValue().newLanguage);
        //
        //
        // // Remove language2, expect REMOVE.
        // removeLanguage(language2);
        // final TimestampedNotification<LanguageImplChange> language2Remove = implObs.poll();
        // assertTrue(language2Remove.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REMOVE, language2, null),
        // language2Remove.notification.getValue());
        //
        //
        // // Remove language3, expect REMOVE and REPLACE_ACTIVE.
        // removeLanguage(language3);
        // final TimestampedNotification<LanguageImplChange> language3Remove = implObs.poll();
        // final TimestampedNotification<LanguageImplChange> language3Replaced = implObs.poll();
        // assertTrue(language3Remove.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REMOVE, language3, null),
        // language3Remove.notification.getValue());
        // assertTrue(language3Replaced.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REPLACE_ACTIVE, language3, language1),
        // language3Replaced.notification.getValue());
        //
        //
        // // Remove language1, expect REMOVE and REMOVE_LAST.
        // removeLanguage(language1);
        // final TimestampedNotification<LanguageImplChange> language1Remove = implObs.poll();
        // final TimestampedNotification<LanguageImplChange> language1Unload = implObs.poll();
        // assertTrue(language1Remove.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REMOVE, language1, null),
        // language1Remove.notification.getValue());
        // assertTrue(language1Unload.notification.isOnNext());
        // assertEquals(new LanguageImplChange(LanguageImplChange.Kind.REMOVE_LAST, language1, null),
        // language1Unload.notification.getValue());


        assertEmpty(compObs);
        assertEmpty(implObs);
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent");
        language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent");

        languageIdentifierService.identify(resourceService.resolve("ram:///Entity/test.ent"));
    }

    @Test(expected = MetaborgRuntimeException.class) public void multipleUnexpectedFacets() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        final ILanguageComponent component =
            language(groupId, "org.metaborg.lang.entity", version, location, "Entity", new DescriptionFacet(
                "Entity language", null), new DescriptionFacet("Entity language", null));
        component.facet(DescriptionFacet.class);
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = resourceService.resolve("ram:///doesnotexist");

        language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        final ILanguageComponent component = language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
        languageService.remove(component);
        languageService.remove(component);
    }
}
