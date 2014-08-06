package org.metaborg.spoofax.core;

import static org.junit.Assert.*;

import java.util.Date;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.language.LanguageFacetChange;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.spoofax.core.service.about.AboutFacet;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;
import org.metaborg.util.observable.TimestampedNotification;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class LanguageServiceTest extends SpoofaxTest {
    @BeforeClass public static void setUpOnce() {
        initialize();
    }


    private LanguageVersion version(int major, int minor, int patch, int qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private FileName location(String uri) throws FileSystemException {
        return session.fileSystemManager.resolveURI(uri);
    }

    private ILanguage
        language(String name, LanguageVersion version, FileName location, ImmutableSet<String> extensions) {
        return session.language.create(name, version, location, extensions);
    }


    @Test public void addSingleLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

        assertEquals(language, session.language.get("Entity"));
        assertSame(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));
        assertSame(language, session.language.get("Entity", version, location));
        assertEquals(1, Iterables.size(session.language.getAll("Entity")));
    }

    @Test public void addDifferentLanguages() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");
        final FileName location3 = location("ram:///Entity3");

        final ILanguage language1 = language("Entity1", version, location1, ImmutableSet.of(".ent1"));
        final ILanguage language2 = language("Entity2", version, location2, ImmutableSet.of(".ent2"));
        final ILanguage language3 = language("Entity3", version, location3, ImmutableSet.of(".ent3"));

        assertEquals(language1, session.language.get("Entity1"));
        assertEquals(language2, session.language.get("Entity2"));
        assertEquals(language3, session.language.get("Entity3"));
        assertEquals(language1, session.language.get("Entity1", version, location1));
        assertEquals(language2, session.language.get("Entity2", version, location2));
        assertEquals(language3, session.language.get("Entity3", version, location3));
        assertEquals(1, Iterables.size(session.language.getAll("Entity1")));
        assertEquals(1, Iterables.size(session.language.getAll("Entity2")));
        assertEquals(1, Iterables.size(session.language.getAll("Entity3")));
    }

    @Test public void addHigherVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 0, 1, 0);
        final LanguageVersion version2 = version(0, 1, 0, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");

        final ILanguage language1 = language("Entity", version1, location1, ImmutableSet.of(".ent"));

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, ImmutableSet.of(".ent"));

        // Language 2 with higher version number becomes active.
        assertEquals(language2, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(session.language.getAll("Entity")));
    }

    @Test public void addLowerVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 1, 0, 0);
        final LanguageVersion version2 = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1/");
        final FileName location2 = location("ram:///Entity2/");

        final ILanguage language1 = language("Entity", version1, location1, ImmutableSet.of(".ent"));

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 = language("Entity", version2, location2, ImmutableSet.of(".ent"));

        // Language 1 with higher version number stays active.
        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(session.language.getAll("Entity")));
    }

    @Test public void mostRecentLanguageActive() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");
        final FileName location3 = location("ram:///Entity3");
        final FileName location4 = location("ram:///Entity3");

        final ILanguage language1 = language("Entity", version, location1, ImmutableSet.of(".ent"));
        assertEquals(language1, session.language.get("Entity"));
        final ILanguage language2 = language("Entity", version, location2, ImmutableSet.of(".ent"));
        assertEquals(language2, session.language.get("Entity"));
        final ILanguage language3 = language("Entity", version, location3, ImmutableSet.of(".ent"));
        assertEquals(language3, session.language.get("Entity"));

        session.language.destroy(language3);
        assertEquals(language2, session.language.get("Entity"));
        session.language.destroy(language1);
        assertEquals(language2, session.language.get("Entity"));
        final ILanguage language4 = language("Entity", version, location4, ImmutableSet.of(".ent"));
        assertEquals(language4, session.language.get("Entity"));
        session.language.destroy(language4);
        assertEquals(language2, session.language.get("Entity"));
        session.language.destroy(language2);
        assertEquals(null, session.language.get("Entity"));
    }

    @Test public void reloadLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

        assertEquals(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));

        language = language("Entity", version, location, ImmutableSet.of(".ent"));

        assertEquals(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));
        assertEquals(1, Iterables.size(session.language.getAll("Entity")));
    }

    @Test public void observables() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");
        final ITestableObserver<LanguageChange> languageObserver = new TestableObserver<LanguageChange>();
        final ITestableObserver<LanguageFacetChange> facetObserver = new TestableObserver<LanguageFacetChange>();

        session.language.changes().subscribe(languageObserver);

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));

        final TimestampedNotification<LanguageChange> loaded = languageObserver.poll();
        final TimestampedNotification<LanguageChange> activated = languageObserver.poll();

        assertTrue(loaded.notification.isOnNext());
        assertEquals(loaded.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.LOADED));
        assertTrue(activated.notification.isOnNext());
        assertEquals(activated.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.ACTIVATED));


        language.facetChanges().subscribe(facetObserver);

        final ILanguageFacet facet = language.addFacet(AboutFacet.class, new AboutFacet("Entity language", null));

        final TimestampedNotification<LanguageFacetChange> added = facetObserver.poll();

        assertTrue(added.notification.isOnNext());
        assertEquals(added.notification.getValue(), new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADDED));

        language.removeFacet(AboutFacet.class);

        final TimestampedNotification<LanguageFacetChange> removed = facetObserver.poll();

        assertTrue(removed.notification.isOnNext());
        assertEquals(removed.notification.getValue(), new LanguageFacetChange(facet, LanguageFacetChange.Kind.REMOVED));

        assertEquals(facetObserver.size(), 0);


        session.language.destroy(language);

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
        final FileName location = location("ram:///");

        language("Entity1", version, location, ImmutableSet.of(".ent1"));
        language("Entity2", version, location, ImmutableSet.of(".ent2"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");

        language("Entity1", version, location1, ImmutableSet.of(".ent"));
        language("Entity2", version, location2, ImmutableSet.of(".ent"));
    }

    @Test(expected = IllegalStateException.class) public void conflictingFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));
        language.addFacet(AboutFacet.class, new AboutFacet("Entity language", null));
        language.addFacet(AboutFacet.class, new AboutFacet("Entity language", null));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        session.language.destroy(new Language("Entity", version, location, ImmutableSet.of(".ent"), new Date()));
    }

    @Test(expected = IllegalStateException.class) public void nonExistantFacet() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        final ILanguage language = language("Entity", version, location, ImmutableSet.of(".ent"));
        language.removeFacet(AboutFacet.class);
    }
}
