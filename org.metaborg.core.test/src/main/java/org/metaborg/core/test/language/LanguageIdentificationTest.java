package org.metaborg.core.test.language;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.metaborg.core.MetaborgModule;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.test.MetaborgTest;

public class LanguageIdentificationTest extends MetaborgTest {


    public LanguageIdentificationTest(MetaborgModule module) {
        super(module);
    }


    @Test public void identification() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");

        final ILanguageComponent component1 =
            language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent1");
        final ILanguageImpl impl1 = component1.contributesTo().iterator().next();
        final ILanguageComponent component2 =
            language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent2");
        final ILanguageImpl impl2 = component2.contributesTo().iterator().next();

        assertTrue(languageIdentifierService.identify(resourceService.resolve("ram:///Entity1/test.ent1"), impl1));
        assertFalse(languageIdentifierService.identify(resourceService.resolve("ram:///Entity2/test.ent2"), impl1));
        assertTrue(languageIdentifierService.identify(resourceService.resolve("ram:///Entity2/test.ent2"), impl2));
        assertFalse(languageIdentifierService.identify(resourceService.resolve("ram:///Entity1/test.ent1"), impl2));
    }

    @Test(expected = IllegalStateException.class) public void conflictingExtension() throws Exception {
        final LanguageVersion version = version(0, 0, 1);
        final FileObject location1 = createDir("ram:///Entity1");
        final FileObject location2 = createDir("ram:///Entity2");

        language(groupId, "org.metaborg.lang.entity1", version, location1, "Entity1", "ent");
        language(groupId, "org.metaborg.lang.entity2", version, location2, "Entity2", "ent");

        languageIdentifierService.identify(resourceService.resolve("ram:///Entity/test.ent"));
    }
}
