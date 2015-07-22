package org.metaborg.spoofax.core.test.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.metaborg.util.test.Assert2.assertIterableEquals;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.test.language.LanguageServiceTest;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.stratego.StrategoFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;

import com.google.common.collect.Iterables;

public class SpoofaxLanguageTest extends LanguageServiceTest {
    public SpoofaxLanguageTest() {
        super(new SpoofaxModule());
    }

    /**
     * The 'res:' filesystem redirects to resources inside the tests' JAR file or class file location, which are copied
     * to the class file location from src/test/resources by Maven. The binary files of the Entity language are located
     * in the resources to test language discovery.
     */
    @Test public void discoverLanguage() throws Exception {
        final FileObject location = resourceService.resolve("res:");

        final Iterable<ILanguageImpl> languages = languageDiscoveryService.discover(location);

        assertEquals(1, Iterables.size(languages));

        final ILanguageImpl language = Iterables.get(languages, 0);

        assertEquals("Entity", language.name());
        assertEquals(resourceService.resolve("res:Entity"), language.location());

        final IdentificationFacet identificationFacet = language.facets(IdentificationFacet.class);
        assertTrue(identificationFacet.identify(resourceService.resolve("ram:///Entity/test.ent")));

        final SyntaxFacet syntaxFacet = language.facets(SyntaxFacet.class);

        assertEquals(resourceService.resolve("res:Entity/include/Entity.tbl"), syntaxFacet.parseTable);
        assertIterableEquals(syntaxFacet.startSymbols, "Start");

        final StrategoFacet strategoFacet = language.facets(StrategoFacet.class);

        assertIterableEquals(strategoFacet.ctreeFiles(), resourceService.resolve("res:Entity/include/entity.ctree"));
        assertIterableEquals(strategoFacet.jarFiles(), resourceService.resolve("res:Entity/include/entity-java.jar"));
        assertEquals("editor-analyze", strategoFacet.analysisStrategy());
        assertEquals("editor-save", strategoFacet.onSaveStrategy());
    }
}
