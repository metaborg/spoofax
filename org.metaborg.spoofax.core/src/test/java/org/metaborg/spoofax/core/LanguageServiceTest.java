package org.metaborg.spoofax.core;

import static org.junit.Assert.*;
import static org.metaborg.util.test.Assert2.*;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.language.LanguageFacetChange;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.spoofax.core.service.about.AboutFacet;
import org.metaborg.spoofax.core.service.actions.ActionsFacet;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.spoofax.core.service.syntax.SyntaxFacet;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;
import org.metaborg.util.observable.TimestampedNotification;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@RunWith(JukitoRunner.class) public class LanguageServiceTest extends SpoofaxTest {
    private LanguageVersion version(int major, int minor, int patch, int qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private FileObject createDirectory(String uri) throws FileSystemException {
        final FileObject file = resourceService.fileSystemManager().resolveFile(uri);
        file.createFolder();
        return file;
    }

    private ILanguage language(String name, LanguageVersion version, FileObject location,
        ImmutableSet<String> extensions) {
        return languageService.create(name, version, location, extensions);
    }


    @Test public void addSingleLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

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

        final ILanguage language1 = language("Entity1", version, location1, ImmutableSet.of(".ent1"));
        final ILanguage language2 = language("Entity2", version, location2, ImmutableSet.of(".ent2"));
        final ILanguage language3 = language("Entity3", version, location3, ImmutableSet.of(".ent3"));

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

        final ILanguage language1 = language("Entity", version1, location1, ImmutableSet.of(".ent"));

        assertEquals(language1, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, ImmutableSet.of(".ent"));

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

        final ILanguage language1 = language("Entity", version1, location1, ImmutableSet.of(".ent"));

        assertEquals(language1, languageService.get("Entity"));
        assertEquals(language1, languageService.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, ImmutableSet.of(".ent"));

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

        final ILanguage language1 = language("Entity", version, location1, ImmutableSet.of(".ent"));
        assertEquals(language1, languageService.get("Entity"));
        final ILanguage language2 = language("Entity", version, location2, ImmutableSet.of(".ent"));
        assertEquals(language2, languageService.get("Entity"));
        final ILanguage language3 = language("Entity", version, location3, ImmutableSet.of(".ent"));
        assertEquals(language3, languageService.get("Entity"));

        languageService.destroy(language3);
        assertEquals(language2, languageService.get("Entity"));
        languageService.destroy(language1);
        assertEquals(language2, languageService.get("Entity"));
        final ILanguage language4 = language("Entity", version, location4, ImmutableSet.of(".ent"));
        assertEquals(language4, languageService.get("Entity"));
        languageService.destroy(language4);
        assertEquals(language2, languageService.get("Entity"));
        languageService.destroy(language2);
        assertEquals(null, languageService.get("Entity"));
    }

    @Test public void reloadLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

        assertEquals(language, languageService.get("Entity"));
        assertEquals(language, languageService.get("Entity", version, location));

        language = language("Entity", version, location, ImmutableSet.of(".ent"));

        assertEquals(language, languageService.get("Entity"));
        assertEquals(language, languageService.get("Entity", version, location));
        assertEquals(1, Iterables.size(languageService.getAll("Entity")));
    }

    /**
     * The 'res:' filesystem redirects to resources inside the tests' JAR file or class file location, which are copied
     * to the class file location from src/test/resources by Maven. The binary files of the Entity language are located
     * in the resources to test language discovery.
     */
    @Test public void discoverLanguage() throws Exception {
        final FileObject location = resourceService.fileSystemManager().resolveFile("res:");

        final Iterable<ILanguage> languages = languageDiscoveryService.discover(location);

        assertEquals(1, Iterables.size(languages));

        final ILanguage language = Iterables.get(languages, 0);

        assertEquals("Entity", language.name());
        assertEquals(resourceService.fileSystemManager().resolveFile("res:Entity"), language.location());
        assertIterableEquals(language.extensions(), "ent");

        final SyntaxFacet syntaxFacet = language.facet(SyntaxFacet.class);

        assertEquals(resourceService.fileSystemManager().resolveFile("res:Entity/include/Entity.tbl"),
            syntaxFacet.parseTable());
        assertIterableEquals(syntaxFacet.startSymbols(), "Start");

        final StrategoFacet strategoFacet = language.facet(StrategoFacet.class);

        assertIterableEquals(strategoFacet.ctreeFiles(),
            resourceService.fileSystemManager().resolveFile("res:Entity/include/entity.ctree"));
        assertIterableEquals(strategoFacet.jarFiles(),
            resourceService.fileSystemManager().resolveFile("res:Entity/include/entity-java.jar"));
        assertEquals("editor-analyze", strategoFacet.analysisStrategy());
        assertEquals("editor-save", strategoFacet.onSaveStrategy());

        final ActionsFacet actionsFacet = language.facet(ActionsFacet.class);
        // TODO: test actions facet.
    }

    @Test public void observables() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");
        final ITestableObserver<LanguageChange> languageObserver = new TestableObserver<LanguageChange>();
        final ITestableObserver<LanguageFacetChange> facetObserver = new TestableObserver<LanguageFacetChange>();

        languageService.changes().subscribe(languageObserver);

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

        final TimestampedNotification<LanguageChange> loaded = languageObserver.poll();
        final TimestampedNotification<LanguageChange> activated = languageObserver.poll();

        assertTrue(loaded.notification.isOnNext());
        assertEquals(loaded.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.LOADED));
        assertTrue(activated.notification.isOnNext());
        assertEquals(activated.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.ACTIVATED));


        language.facetChanges().subscribe(facetObserver);

        final ILanguageFacet facet = language.addFacet(new AboutFacet("Entity language", null));

        final TimestampedNotification<LanguageFacetChange> added = facetObserver.poll();

        assertTrue(added.notification.isOnNext());
        assertEquals(added.notification.getValue(), new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADDED));

        language.removeFacet(AboutFacet.class);

        final TimestampedNotification<LanguageFacetChange> removed = facetObserver.poll();

        assertTrue(removed.notification.isOnNext());
        assertEquals(removed.notification.getValue(), new LanguageFacetChange(facet, LanguageFacetChange.Kind.REMOVED));

        assertEquals(facetObserver.size(), 0);


        languageService.destroy(language);

        final TimestampedNotification<LanguageChange> deactivated = languageObserver.poll();
        final TimestampedNotification<LanguageChange> unloaded = languageObserver.poll();

        assertTrue(deactivated.notification.isOnNext());
        assertEquals(deactivated.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.DEACTIVATED));
        assertTrue(unloaded.notification.isOnNext());
        assertEquals(unloaded.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.UNLOADED));

        assertEquals(languageObserver.size(), 0);
    }

    @Test(expected = IllegalStateException.class) public void conflictingLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        language("Entity1", version, location, ImmutableSet.of(".ent1"));
        language("Entity2", version, location, ImmutableSet.of(".ent2"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location1 = createDirectory("ram:///Entity1");
        final FileObject location2 = createDirectory("ram:///Entity2");

        language("Entity1", version, location1, ImmutableSet.of(".ent"));
        language("Entity2", version, location2, ImmutableSet.of(".ent"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));
        language.addFacet(new AboutFacet("Entity language", null));
        language.addFacet(new AboutFacet("Entity language", null));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = resourceService.fileSystemManager().resolveFile("ram:///doesnotexist");

        language("Entity", version, location, ImmutableSet.of(".ent"));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        languageService.destroy(new Language("Entity", version, location, ImmutableSet.of(".ent"), new Date()));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileObject location = createDirectory("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));
        language.removeFacet(AboutFacet.class);
    }
}
