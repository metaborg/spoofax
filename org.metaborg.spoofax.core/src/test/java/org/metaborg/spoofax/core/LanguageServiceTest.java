package org.metaborg.spoofax.core;

import static org.junit.Assert.*;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.observable.ITestableObserver;
import org.metaborg.util.observable.TestableObserver;
import org.metaborg.util.observable.TimestampedNotification;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class LanguageServiceTest extends SpoofaxTest {
    @BeforeClass
    public static void setUpOnce() {
        initialize();
    }


    private LanguageVersion version(int major, int minor, int patch, int qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private FileName location(String uri) throws FileSystemException {
        return session.fileSystemManager.resolveURI(uri);
    }

    private ILanguage language(String name, LanguageVersion version, FileName location,
        ImmutableSet<String> extensions, Iterable<FileObject> resources) {
        return session.language.create(name, version, location, extensions, resources);
    }


    @Test
    public void addSingleLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        final ILanguage language =
            language("Entity", version, location, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        assertEquals(language, session.language.get("Entity"));
        assertSame(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));
        assertSame(language, session.language.get("Entity", version, location));
        assertEquals(1, Iterables.size(session.language.getAll("Entity")));
    }

    @Test
    public void addDifferentLanguages() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");
        final FileName location3 = location("ram:///Entity3");

        final ILanguage language1 =
            language("Entity1", version, location1, ImmutableSet.of(".ent1"), Iterables2.<FileObject>empty());
        final ILanguage language2 =
            language("Entity2", version, location2, ImmutableSet.of(".ent2"), Iterables2.<FileObject>empty());
        final ILanguage language3 =
            language("Entity3", version, location3, ImmutableSet.of(".ent3"), Iterables2.<FileObject>empty());

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

    @Test
    public void addHigherVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 0, 1, 0);
        final LanguageVersion version2 = version(0, 1, 0, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");

        final ILanguage language1 =
            language("Entity", version1, location1, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 =
            language("Entity", version2, location2, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        // Language 2 with higher version number becomes active.
        assertEquals(language2, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(session.language.getAll("Entity")));
    }

    @Test
    public void addLowerVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 1, 0, 0);
        final LanguageVersion version2 = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1/");
        final FileName location2 = location("ram:///Entity2/");

        final ILanguage language1 =
            language("Entity", version1, location1, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 =
            language("Entity", version2, location2, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        // Language 1 with higher version number stays active.
        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
        assertEquals(2, Iterables.size(session.language.getAll("Entity")));
    }

    @Test
    public void mostRecentLanguageActive() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");
        final FileName location3 = location("ram:///Entity3");
        final FileName location4 = location("ram:///Entity3");

        final ILanguage language1 =
            language("Entity", version, location1, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
        assertEquals(language1, session.language.get("Entity"));
        final ILanguage language2 =
            language("Entity", version, location2, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
        assertEquals(language2, session.language.get("Entity"));
        final ILanguage language3 =
            language("Entity", version, location3, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
        assertEquals(language3, session.language.get("Entity"));

        session.language.remove(language3);
        assertEquals(language2, session.language.get("Entity"));
        session.language.remove(language1);
        assertEquals(language2, session.language.get("Entity"));
        final ILanguage language4 =
            language("Entity", version, location4, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
        assertEquals(language4, session.language.get("Entity"));
        session.language.remove(language4);
        assertEquals(language2, session.language.get("Entity"));
        session.language.remove(language2);
        assertEquals(null, session.language.get("Entity"));
    }

    @Test
    public void reloadLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        ILanguage language =
            language("Entity", version, location, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        assertEquals(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));

        language = language("Entity", version, location, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        assertEquals(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));
        assertEquals(1, Iterables.size(session.language.getAll("Entity")));
    }

    @Test
    public void observables() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");
        final ITestableObserver<LanguageChange> observer = new TestableObserver<LanguageChange>();

        session.language.changes().subscribe(observer);

        final ILanguage language =
            language("Entity", version, location, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());

        final TimestampedNotification<LanguageChange> loaded = observer.poll();
        final TimestampedNotification<LanguageChange> activated = observer.poll();

        assertTrue(loaded.notification.isOnNext());
        assertEquals(loaded.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.LOADED));
        assertTrue(activated.notification.isOnNext());
        assertEquals(activated.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.ACTIVATED));

        session.language.remove(language);

        final TimestampedNotification<LanguageChange> deactivated = observer.poll();
        final TimestampedNotification<LanguageChange> unloaded = observer.poll();

        assertTrue(deactivated.notification.isOnNext());
        assertEquals(deactivated.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.DEACTIVATED));
        assertTrue(unloaded.notification.isOnNext());
        assertEquals(unloaded.notification.getValue(), new LanguageChange(language, LanguageChange.Kind.UNLOADED));
    }

    @Test(expected = IllegalStateException.class)
    public void conflictingLocation() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        language("Entity1", version, location, ImmutableSet.of(".ent1"), Iterables2.<FileObject>empty());
        language("Entity2", version, location, ImmutableSet.of(".ent2"), Iterables2.<FileObject>empty());
    }

    @Test(expected = IllegalStateException.class)
    public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1");
        final FileName location2 = location("ram:///Entity2");

        language("Entity1", version, location1, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
        language("Entity2", version, location2, ImmutableSet.of(".ent"), Iterables2.<FileObject>empty());
    }
}
