package org.metaborg.spoofax.core;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Sets;

public class LanguageServiceTest extends SpoofaxTest {
    private LanguageVersion version(int major, int minor, int patch, int qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private FileName location(String uri) throws FileSystemException {
        return session.fileSystemManager.resolveURI(uri);
    }

    private ILanguage language(String name, LanguageVersion version, FileName location, Set<String> extensions,
        Iterable<FileObject> resources) {
        return session.language.create(name, version, location, extensions, resources);
    }

    @Test
    public void addSingleLanguage() throws Exception {
        final LanguageVersion version = version(0, 0, 1, 0);
        final FileName location = location("ram:///");

        final ILanguage language =
            language("Entity", version, location, Sets.newHashSet(".ent", ".entity"), Iterables2.<FileObject>empty());

        assertEquals(language, session.language.get("Entity"));
        assertSame(language, session.language.get("Entity"));
        assertEquals(language, session.language.get("Entity", version, location));
        assertSame(language, session.language.get("Entity", version, location));
    }

    @Test
    public void addHigherVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 0, 1, 0);
        final LanguageVersion version2 = version(0, 1, 0, 0);
        final FileName location1 = location("ram:///Entity1/");
        final FileName location2 = location("ram:///Entity2/");

        final ILanguage language1 =
            language("Entity", version1, location1, Sets.newHashSet(".ent", ".entity"), Iterables2.<FileObject>empty());

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 =
            language("Entity", version2, location2, Sets.newHashSet(".ent", ".entity"), Iterables2.<FileObject>empty());

        assertEquals(language2, session.language.get("Entity")); // Language 2 becomes active.
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
    }

    @Test
    public void addLowerVersionLanguage() throws Exception {
        final LanguageVersion version1 = version(0, 1, 0, 0);
        final LanguageVersion version2 = version(0, 0, 1, 0);
        final FileName location1 = location("ram:///Entity1/");
        final FileName location2 = location("ram:///Entity2/");

        final ILanguage language1 =
            language("Entity", version1, location1, Sets.newHashSet(".ent", ".entity"), Iterables2.<FileObject>empty());

        assertEquals(language1, session.language.get("Entity"));
        assertEquals(language1, session.language.get("Entity", version1, location1));

        final ILanguage language2 =
            language("Entity", version2, location2, Sets.newHashSet(".ent", ".entity"), Iterables2.<FileObject>empty());

        assertEquals(language1, session.language.get("Entity")); // Language 1 stays active.
        assertEquals(language1, session.language.get("Entity", version1, location1));
        assertEquals(language2, session.language.get("Entity", version2, location2));
    }

    // @Test
    public void reloadLanguage() {

    }

    // @Test
    public void conflictingLocation() {

    }

    // @Test
    public void conflictingExtension() {

    }
}
