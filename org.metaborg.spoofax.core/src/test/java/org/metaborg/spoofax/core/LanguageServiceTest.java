package org.metaborg.spoofax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.metaborg.util.test.Assert2.assertIterableEquals;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.language.DescriptionFacet;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.language.IdentificationFacet;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.language.LanguageFacetChange;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.spoofax.core.language.ResourceExtensionsIdentifier;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;
import org.metaborg.util.observable.TimestampedNotification;

import com.google.common.collect.Iterables;

public class LanguageServiceTest extends SpoofaxTest {
    private LanguageVersion version(int major, int minor, int patch, int qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private FileObject createDirectory(String uri) throws FileSystemException {
        final FileObject file = resourceService.resolve(uri);
        file.createFolder();
        return file;
    }

    private ILanguage language(String name, LanguageVersion version, FileObject location, String id) {
        final ILanguage language = languageService.create(name, version, location, id);
        languageService.add(language);
        return language;
    }

    private ILanguage language(String name, LanguageVersion version, FileObject location, String id,
        String... extensions) {
        final ILanguage language = language(name, version, location, id);
        final IdentificationFacet identificationFacet =
            new IdentificationFacet(new ResourceExtensionsIdentifier(Iterables2.from(extensions)));
        language.addFacet(identificationFacet);
        return language;
    }

    private void removeLanguage(ILanguage language) {
        languageService.remove(language);
    }


    @Test public void addSingleLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, "org.metaborg.lang.entity");

        assertEquals(language, languageService.get("Entity"));
        assertSame(language, languageService.get("Entity"));
        assertEquals(language, languageService.get("Entity", version, location));
        assertSame(language, languageService.get("Entity", version, location));
        assertEquals(1, Iterables.size(languageService.getAll("Entity")));
    }

    @Test public void addDifferentLanguages() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");

        final ILanguage language1 = language("Entity1", version, location1, "org.metaborg.lang.entity1");
        final ILanguage language2 = language("Entity2", version, location2, "org.metaborg.lang.entity2");
        final ILanguage language3 = language("Entity3", version, location3, "org.metaborg.lang.entity3");

        assertEquals(language1, languageService.get("Entity1"));
        assertEquals(language2, languageService.get("Entity2"));
        assertEquals(language3, languageService.get("Entity3"));
        assertEquals(language1, languageService.get("Entity1", version, location1));
        assertEquals(language2, languageService.get("Entity2", version, location2));
        assertEquals(language3, languageService.get("Entity3", version, location3));
        assertEquals(1, Iterables.size(languageService.getAll("Entity1")));
        assertEquals(1, Iterables.size(languageService.getAll("Entity2")));
        assertEquals(1, Iterables.size(languageService.getAll("Entity3")));
    }

    @Test public void addHigherVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 0, 1, 0);
        final LanguageVersion version2 = version(0, 1, 0, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        final ILanguage language1 = language("Entity", version1, location1, "org.metaborg.lang.entity");

        assertEquals(language1, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, "org.metaborg.lang.entity");

        // Language 2 with higher version number becomes active.
        assertEquals(language2, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));
        assertEquals(language2, languageService.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(languageService.getAll("Entity")));
    }

    @Test public void addLowerVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 1, 0, 0);
        final LanguageVersion version2 = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1/");
        final FileObject location2 = createDirectory("ram:///Entity2/");

        final ILanguage language1 = language("Entity", version1, location1, "org.metaborg.lang.entity");

        assertEquals(language1, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, "org.metaborg.lang.entity");

        // Language 1 with higher version number stays active.
        assertEquals(language1, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));
        assertEquals(language2, languageService.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(languageService.getAll("Entity")));
    }

    @Test public void mostRecentLanguageActive() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final FileObject location4 = createDirectory("ram:///Entity3");

        final ILanguage language1 = language("Entity", version, location1, "org.metaborg.lang.entity");
        assertEquals(language1, languageService.get("Entity"));
        final ILanguage language2 = language("Entity", version, location2, "org.metaborg.lang.entity");
        assertEquals(language2, languageService.get("Entity"));
        final ILanguage language3 = language("Entity", version, location3, "org.metaborg.lang.entity");
        assertEquals(language3, languageService.get("Entity"));

        languageService.remove(language3);
        assertEquals(language2, languageService.get("Entity"));
        languageService.remove(language1);
        assertEquals(language2, languageService.get("Entity"));
        final ILanguage language4 = language("Entity", version, location4, "org.metaborg.lang.entity");
        assertEquals(language4, languageService.get("Entity"));
        languageService.remove(language4);
        assertEquals(language2, languageService.get("Entity"));
        languageService.remove(language2);
        assertEquals(null, languageService.get("Entity"));
    }

    @Test public void reloadLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        ILanguage language = language("Entity", version, location, "org.metaborg.lang.entity");

        assertEquals(language, languageService.get("Entity"));
        assertEquals(language, languageService.get("Entity", version, location));

        language = language("Entity", version, location, "org.metaborg.lang.entity");

        assertEquals(language, languageService.get("Entity"));
        assertEquals(language, languageService.get("Entity", version, location));
        assertEquals(1, Iterables.size(languageService.getAll("Entity")));
    }

    @Test public void identification() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        final ILanguage language1 = language("Entity1", version, location1, "org.metaborg.lang.entity1", "ent1");
        final ILanguage language2 = language("Entity2", version, location2, "org.metaborg.lang.entity2", "ent2");

        final IdentificationFacet identificationFacet1 = language1.facet(IdentificationFacet.class);
        assertTrue(identificationFacet1.identify(resourceService.resolve("ram:///Entity1/test.ent1")));
        assertFalse(identificationFacet1.identify(resourceService.resolve("ram:///Entity2/test.ent2")));

        final IdentificationFacet identificationFacet2 = language2.facet(IdentificationFacet.class);
        assertTrue(identificationFacet2.identify(resourceService.resolve("ram:///Entity2/test.ent2")));
        assertFalse(identificationFacet2.identify(resourceService.resolve("ram:///Entity1/test.ent1")));
    }

    /**
     * The 'res:' filesystem redirects to resources inside the tests' JAR file or class file location, which are copied
     * to the class file location from src/test/resources by Maven. The binary files of the Entity language are located
     * in the resources to test language discovery.
     */
    @Test public void discoverLanguage() throws Exception {
        final FileObject location = resourceService.resolve("res:");

        final Iterable<ILanguage> languages = languageDiscoveryService.discover(location);

        assertEquals(1, Iterables.size(languages));

        final ILanguage language = Iterables.get(languages, 0);

        assertEquals("Entity", language.name());
        assertEquals(resourceService.resolve("res:Entity"), language.location());

        final IdentificationFacet identificationFacet = language.facet(IdentificationFacet.class);
        assertTrue(identificationFacet.identify(resourceService.resolve("ram:///Entity/test.ent")));

        final SyntaxFacet syntaxFacet = language.facet(SyntaxFacet.class);

        assertEquals(resourceService.resolve("res:Entity/include/Entity.tbl"), syntaxFacet.parseTable);
        assertIterableEquals(syntaxFacet.startSymbols, "Start");

        final StrategoFacet strategoFacet = language.facet(StrategoFacet.class);

        assertIterableEquals(strategoFacet.ctreeFiles(), resourceService.resolve("res:Entity/include/entity.ctree"));
        assertIterableEquals(strategoFacet.jarFiles(), resourceService.resolve("res:Entity/include/entity-java.jar"));
        assertEquals("editor-analyze", strategoFacet.analysisStrategy());
        assertEquals("editor-save", strategoFacet.onSaveStrategy());
    }

    @Test public void observables() throws Exception {
        final LanguageVersion version1 = version(0, 0, 1, 0);
        final LanguageVersion version2 = version(0, 0, 2, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");
        final FileObject location3 = createDirectory("ram:///Entity3");
        final ITestableObserver<LanguageChange> languageObserver = new TestableObserver<LanguageChange>();
        final ITestableObserver<LanguageFacetChange> facetObserver = new TestableObserver<LanguageFacetChange>();

        languageService.changes().subscribe(languageObserver);


        // Add language, expect ADD_FIRST and ADD.
        final ILanguage language1 = language("Entity", version1, location1, "org.metaborg.lang.entity");
        final TimestampedNotification<LanguageChange> language1Load = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language1Add = languageObserver.poll();
        assertTrue(language1Load.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD_FIRST, null, language1),
            language1Load.notification.getValue());
        assertTrue(language1Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language1), language1Add.notification.getValue());


        // Create and remove facet, expect ADD and REMOVE.
        language1.facetChanges().subscribe(facetObserver);
        final ILanguageFacet facet = language1.addFacet(new DescriptionFacet("Entity language", null));
        final TimestampedNotification<LanguageFacetChange> addedFacet = facetObserver.poll();
        assertTrue(addedFacet.notification.isOnNext());
        assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADD), addedFacet.notification.getValue());

        language1.removeFacet(DescriptionFacet.class);
        final TimestampedNotification<LanguageFacetChange> removed = facetObserver.poll();
        assertTrue(removed.notification.isOnNext());
        assertEquals(new LanguageFacetChange(facet, LanguageFacetChange.Kind.REMOVE), removed.notification.getValue());
        assertEquals(facetObserver.size(), 0);


        // Add language2 with same name and version, but different location. Expect ADD and REPLACE_ACTIVE.
        final ILanguage language2 = language("Entity", version1, location2, "org.metaborg.lang.entity");
        final TimestampedNotification<LanguageChange> language2Add = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language2Replace = languageObserver.poll();
        assertTrue(language2Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language2), language2Add.notification.getValue());
        assertTrue(language2Replace.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language1, language2),
            language2Replace.notification.getValue());


        // Add language2 again, expect RELOAD_ACTIVE.
        final ILanguage language2Again = language("Entity", version1, location2, "org.metaborg.lang.entity");
        final TimestampedNotification<LanguageChange> language2ReloadActive = languageObserver.poll();
        assertEquals(language2, language2Again);
        assertNotSame(language2, language2Again);
        assertTrue(language2ReloadActive.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.RELOAD_ACTIVE, language2, language2Again),
            language2ReloadActive.notification.getValue());
        assertSame(language2, language2ReloadActive.notification.getValue().oldLanguage);
        assertSame(language2Again, language2ReloadActive.notification.getValue().newLanguage);


        // Add language3 with same name, but higher version and different location. Expect ADD and REPLACE_ACTIVE.
        final ILanguage language3 = language("Entity", version2, location3, "org.metaborg.lang.entity");
        final TimestampedNotification<LanguageChange> language3Add = languageObserver.poll();
        final TimestampedNotification<LanguageChange> language3Replace = languageObserver.poll();
        assertTrue(language3Add.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.ADD, null, language3), language3Add.notification.getValue());
        assertTrue(language3Replace.notification.isOnNext());
        assertEquals(new LanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language2, language3),
            language3Replace.notification.getValue());


        // Add language2 again, expect RELOAD.
        final ILanguage language2AgainAgain = language("Entity", version1, location2, "org.metaborg.lang.entity");
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
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        language("Entity1", version, location, "org.metaborg.lang.entity1");
        language("Entity2", version, location, "org.metaborg.lang.entity2");
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        language("Entity1", version, location1, "org.metaborg.lang.entity1", "ent");
        language("Entity2", version, location2, "org.metaborg.lang.entity2", "ent");

        languageIdentifierService.identify(resourceService.resolve("ram:///Entity/test.ent"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, "org.metaborg.lang.entity");
        language.addFacet(new DescriptionFacet("Entity language", null));
        language.addFacet(new DescriptionFacet("Entity language", null));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = resourceService.resolve("ram:///doesnotexist");

        language("Entity", version, location, "org.metaborg.lang.entity");
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        languageService.remove(new Language("Entity", location, version, 0, "org.metaborg.lang.entity"));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, "org.metaborg.lang.entity");
        language.removeFacet(DescriptionFacet.class);
    }
}
