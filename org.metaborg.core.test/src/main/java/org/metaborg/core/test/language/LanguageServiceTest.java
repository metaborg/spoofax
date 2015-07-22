package org.metaborg.core.test.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.metaborg.core.language.DescriptionFacet;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.Language;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.language.LanguageFacetChange;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.test.MetaborgTest;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;
import org.metaborg.util.observable.TimestampedNotification;

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

    private ILanguageImpl language(String groupId, String id, LanguageVersion version, FileObject location, String name) {
        return language(new LanguageIdentifier(groupId, id, version), location, name);
    }

    private ILanguageImpl language(LanguageIdentifier identifier, FileObject location, String name) {
        final ILanguageImpl language = languageService.create(identifier, location, name);
        languageService.add(language);
        return language;
    }

    private ILanguageImpl language(String groupId, String id, LanguageVersion version, FileObject location, String name,
        String... extensions) {
        return language(new LanguageIdentifier(groupId, id, version), location, name, extensions);
    }

    private ILanguageImpl language(LanguageIdentifier identifier, FileObject location, String name, String... extensions) {
        final ILanguageImpl language = language(identifier, location, name);
        final IdentificationFacet identificationFacet =
            new IdentificationFacet(new ResourceExtensionsIdentifier(Iterables2.from(extensions)));
        language.addFacet(identificationFacet);
        return language;
    }

    private void removeLanguage(ILanguageImpl language) {
        languageService.remove(language);
    }


    @Test public void addSingleLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);
        final FileObject location = createDirectory("ram:///");
        final String name = "Entity";

        final ILanguageImpl language = language(identifier, location, name);

        assertEquals(language, languageService.get(identifier));
        assertSame(language, languageService.get(identifier));
        assertEquals(language, languageService.get(groupId, id));
        assertSame(language, languageService.get(groupId, id));
        assertEquals(language, languageService.get(name));
        assertSame(language, languageService.get(name));
        assertEquals(language, languageService.get(location.getName()));
        assertSame(language, languageService.get(location.getName()));
        assertEquals(1, Iterables.size(languageService.getAll(identifier)));
        assertEquals(1, Iterables.size(languageService.getAll(name)));
        assertEquals(1, Iterables.size(languageService.getAllActive()));
        assertEquals(1, Iterables.size(languageService.getAll()));
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

        final ILanguageImpl language1 = language(identifier1, location1, name1);
        final ILanguageImpl language2 = language(identifier2, location2, name2);
        final ILanguageImpl language3 = language(identifier3, location3, name3);

        assertEquals(language1, languageService.get(identifier1));
        assertSame(language1, languageService.get(identifier1));
        assertEquals(language2, languageService.get(identifier2));
        assertSame(language2, languageService.get(identifier2));
        assertEquals(language3, languageService.get(identifier3));
        assertSame(language3, languageService.get(identifier3));
        assertEquals(language1, languageService.get(groupId, id1));
        assertSame(language1, languageService.get(groupId, id1));
        assertEquals(language2, languageService.get(groupId, id2));
        assertSame(language2, languageService.get(groupId, id2));
        assertEquals(language3, languageService.get(groupId, id3));
        assertSame(language3, languageService.get(groupId, id3));
        assertEquals(language1, languageService.get(name1));
        assertSame(language1, languageService.get(name1));
        assertEquals(language2, languageService.get(name2));
        assertSame(language2, languageService.get(name2));
        assertEquals(language3, languageService.get(name3));
        assertSame(language3, languageService.get(name3));
        assertEquals(language1, languageService.get(location1.getName()));
        assertSame(language1, languageService.get(location1.getName()));
        assertEquals(language2, languageService.get(location2.getName()));
        assertSame(language2, languageService.get(location2.getName()));
        assertEquals(language3, languageService.get(location3.getName()));
        assertSame(language3, languageService.get(location3.getName()));
        assertEquals(1, Iterables.size(languageService.getAll(identifier1)));
        assertEquals(1, Iterables.size(languageService.getAll(identifier2)));
        assertEquals(1, Iterables.size(languageService.getAll(identifier3)));
        assertEquals(1, Iterables.size(languageService.getAll(name1)));
        assertEquals(1, Iterables.size(languageService.getAll(name2)));
        assertEquals(1, Iterables.size(languageService.getAll(name3)));
        assertEquals(3, Iterables.size(languageService.getAllActive()));
        assertEquals(3, Iterables.size(languageService.getAll()));
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

        final ILanguageImpl language1 = language(identifier1, location1, name);

        assertEquals(language1, languageService.get(identifier1));
        assertEquals(language1, languageService.get(groupId, id));
        assertEquals(language1, languageService.get(location1.getName()));
        assertEquals(language1, languageService.get(name));

        final ILanguageImpl language2 = language(identifier2, location2, name);

        // Language 2 with higher version number becomes active.
        assertEquals(language1, languageService.get(identifier1));
        assertEquals(language2, languageService.get(identifier2));
        assertEquals(language2, languageService.get(groupId, id));
        assertEquals(language1, languageService.get(location1.getName()));
        assertEquals(language2, languageService.get(location2.getName()));
        assertEquals(language2, languageService.get(name));
        assertEquals(1, Iterables.size(languageService.getAll(identifier1)));
        assertEquals(1, Iterables.size(languageService.getAll(identifier2)));
        assertEquals(2, Iterables.size(languageService.getAll(name)));
        assertEquals(1, Iterables.size(languageService.getAllActive()));
        assertEquals(2, Iterables.size(languageService.getAll()));
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

        final ILanguageImpl language1 = language(identifier1, location1, name);

        assertEquals(language1, languageService.get(identifier1));
        assertEquals(language1, languageService.get(groupId, id));
        assertEquals(language1, languageService.get(location1.getName()));
        assertEquals(language1, languageService.get(name));

        final ILanguageImpl language2 = language(identifier2, location2, name);

        // Language 1 with higher version number stays active.
        assertEquals(language1, languageService.get(identifier1));
        assertEquals(language2, languageService.get(identifier2));
        assertEquals(language1, languageService.get(groupId, id));
        assertEquals(language1, languageService.get(location1.getName()));
        assertEquals(language2, languageService.get(location2.getName()));
        assertEquals(language1, languageService.get(name));
        assertEquals(1, Iterables.size(languageService.getAll(identifier1)));
        assertEquals(1, Iterables.size(languageService.getAll(identifier2)));
        assertEquals(2, Iterables.size(languageService.getAll(name)));
        assertEquals(1, Iterables.size(languageService.getAllActive()));
        assertEquals(2, Iterables.size(languageService.getAll()));
    }

    @Test public void mostRecentLanguageActive() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final FileObject location4 = createDirectory("ram:///Entity3");
        final String name = "Entity";

        final ILanguageImpl language1 = language(groupId, id, version, location1, name);
        assertEquals(language1, languageService.get(name));
        final ILanguageImpl language2 = language(groupId, id, version, location2, name);
        assertEquals(language2, languageService.get(name));
        final ILanguageImpl language3 = language(groupId, id, version, location3, name);
        assertEquals(language3, languageService.get(name));

        languageService.remove(language3);
        assertEquals(language2, languageService.get(name));
        languageService.remove(language1);
        assertEquals(language2, languageService.get(name));
        final ILanguageImpl language4 = language(groupId, id, version, location4, name);
        assertEquals(language4, languageService.get(name));
        languageService.remove(language4);
        assertEquals(language2, languageService.get(name));
        languageService.remove(language2);
        assertEquals(null, languageService.get(name));
    }

    @Test public void reloadLanguage() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");
        final String name = "Entity";

        ILanguageImpl language = language(groupId, id, version, location, name);

        assertEquals(language, languageService.get(name));

        language = language(groupId, id, version, location, name);

        assertEquals(language, languageService.get(name));
        assertEquals(1, Iterables.size(languageService.getAll(name)));
    }

    @Test public void identification() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        final ILanguageImpl language1 =
            language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent1");
        final ILanguageImpl language2 =
            language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent2");

        final IdentificationFacet identificationFacet1 = language1.facets(IdentificationFacet.class);
        assertTrue(identificationFacet1.identify(resourceService.resolve("ram:///Entity1/test.ent1")));
        assertFalse(identificationFacet1.identify(resourceService.resolve("ram:///Entity2/test.ent2")));

        final IdentificationFacet identificationFacet2 = language2.facets(IdentificationFacet.class);
        assertTrue(identificationFacet2.identify(resourceService.resolve("ram:///Entity2/test.ent2")));
        assertFalse(identificationFacet2.identify(resourceService.resolve("ram:///Entity1/test.ent1")));
    }

    @Test public void observables() throws Exception {
        final String id = "org.metaborg.lang.entity";
        final LanguageVersion version1 = version(0, 0, 1);
        final LanguageVersion version2 = version(0, 0, 2);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final ITestableObserver<LanguageChange> languageObserver = new TestableObserver<LanguageChange>();
        final ITestableObserver<LanguageFacetChange> facetObserver = new TestableObserver<LanguageFacetChange>();
        final String name = "Entity";

        languageService.changes().subscribe(languageObserver);


        // Add language, expect ADD_FIRST and ADD.
        final ILanguageImpl language1 = language(groupId, id, version1, location1, name);
        final TimestampedNotification<LanguageChange> language1Load = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language1Add = languageObserver.poll();
        assertTrue(language1Load.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD_FIRST, null, language1),
            language1Load.notification.getValue());
        assertTrue(language1Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language1), language1Add.notification.getValue());


        // Create and remove facet, expect ADD and REMOVE.
        language1.facetChanges().subscribe(facetObserver);
        final IFacet facet = language1.addFacet(new DescriptionFacet("Entity language", null));
        final TimestampedNotification<LanguageFacetChange> addedFacet = facetObserver.poll();
        assertTrue(addedFacet.notification.isOnNext());
        assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADD), addedFacet.notification.getValue());

        language1.removeFacet(DescriptionFacet.class);
        final TimestampedNotification<LanguageFacetChange> removed = facetObserver.poll();
        assertTrue(removed.notification.isOnNext());
        assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.REMOVE), removed.notification.getValue());
        assertEquals(facetObserver.size(), 0);


        // Add language2 with same name and version, but different location. Expect ADD and REPLACE_ACTIVE.
        final ILanguageImpl language2 = language(groupId, id, version1, location2, name);
        final TimestampedNotification<LanguageChange> language2Add = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language2Replace = languageObserver.poll();
        assertTrue(language2Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language2), language2Add.notification.getValue());
        assertTrue(language2Replace.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language1, language2),
            language2Replace.notification.getValue());


        // Add language2 again, expect RELOAD_ACTIVE.
        final ILanguageImpl language2Again = language(groupId, id, version1, location2, name);
        final TimestampedNotification<LanguageChange> language2ReloadActive = languageObserver.poll();
        assertEquals(language2, language2Again);
        assertNotSame(language2, language2Again);
        assertTrue(language2ReloadActive.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.RELOAD_ACTIVE, language2, language2Again),
            language2ReloadActive.notification.getValue());
        assertSame(language2, language2ReloadActive.notification.getValue().oldLanguage);
        assertSame(language2Again, language2ReloadActive.notification.getValue().newLanguage);


        // Add language3 with same name, but higher version and different location. Expect ADD and REPLACE_ACTIVE.
        final ILanguageImpl language3 = language(groupId, id, version2, location3, name);
        final TimestampedNotification<LanguageChange> language3Add = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language3Replace = languageObserver.poll();
        assertTrue(language3Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language3), language3Add.notification.getValue());
        assertTrue(language3Replace.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language2, language3),
            language3Replace.notification.getValue());


        // Add language2 again, expect RELOAD.
        final ILanguageImpl language2AgainAgain = language(groupId, id, version1, location2, name);
        final TimestampedNotification<LanguageChange> language2Reload = languageObserver.poll();
        assertEquals(language2Again, language2AgainAgain);
        assertNotSame(language2Again, language2AgainAgain);
        assertNotSame(language2, language2AgainAgain);
        assertTrue(language2Reload.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.RELOAD, language2Again, language2AgainAgain),
            language2Reload.notification.getValue());
        assertSame(language2Again, language2Reload.notification.getValue().oldLanguage);
        assertSame(language2AgainAgain, language2Reload.notification.getValue().newLanguage);


        // Remove language2, expect REMOVE.
        removeLanguage(language2);
        final TimestampedNotification<LanguageChange> language2Remove = languageObserver.poll();
        assertTrue(language2Remove.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REMOVE, language2, null),
            language2Remove.notification.getValue());


        // Remove language3, expect REMOVE and REPLACE_ACTIVE.
        removeLanguage(language3);
        final TimestampedNotification<LanguageChange> language3Remove = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language3Replaced = languageObserver.poll();
        assertTrue(language3Remove.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REMOVE, language3, null),
            language3Remove.notification.getValue());
        assertTrue(language3Replaced.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language3, language1),
            language3Replaced.notification.getValue());


        // Remove language1, expect REMOVE and REMOVE_LAST.
        removeLanguage(language1);
        final TimestampedNotification<LanguageChange> language1Remove = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language1Unload = languageObserver.poll();
        assertTrue(language1Remove.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REMOVE, language1, null),
            language1Remove.notification.getValue());
        assertTrue(language1Unload.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REMOVE_LAST, language1, null),
            language1Unload.notification.getValue());


        assertEquals(languageObserver.size(), 0);
    }

    @Test(expected = IllegalStateException.class) public void conflictingLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        language(groupId, "org.metaborg.lang.entity1", version, location, "Entity1");
        language(groupId, "org.metaborg.lang.entity2", version, location, "Entity2");
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent");
        language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent");

        languageIdentifierService.identify(resourceService.resolve("ram:///Entity/test.ent"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        final ILanguageImpl language = language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
        language.addFacet(new DescriptionFacet("Entity language", null));
        language.addFacet(new DescriptionFacet("Entity language", null));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = resourceService.resolve("ram:///doesnotexist");

        language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        languageService.remove(new Language(new LanguageIdentifier(groupId, "org.metaborg.lang.entity", version),
            location, "Entity", 0));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location = createDirectory("ram:///");

        final ILanguageImpl language = language(groupId, "org.metaborg.lang.entity", version, location, "Entity");
        language.removeFacet(DescriptionFacet.class);
    }
}
