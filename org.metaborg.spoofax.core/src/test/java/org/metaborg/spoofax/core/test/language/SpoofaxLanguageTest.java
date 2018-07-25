package org.metaborg.spoofax.core.test.language;

import static org.junit.Assert.*;
import static org.metaborg.util.test.Assert2.assertIterableEquals;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.test.language.LanguageServiceTest;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.semantic_provider.SemanticProviderFacet;
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

        final Iterable<ILanguageComponent> languages = languageDiscoveryService.discover(languageDiscoveryService.request(location));

        assertEquals(1, Iterables.size(languages));

        final ILanguageComponent component = Iterables.get(languages, 0);
        final ILanguageImpl impl = Iterables.get(component.contributesTo(), 0);
        final ILanguage language = impl.belongsTo();

        assertEquals("Entity", language.name());
        assertEquals(resourceService.resolve("res:Entity"), component.location());

        final IdentificationFacet identificationFacet = impl.facet(IdentificationFacet.class);

        assertTrue(identificationFacet.identify(resourceService.resolve("ram:///Entity/test.ent")));

        final SyntaxFacet syntaxFacet = impl.facet(SyntaxFacet.class);

        assertEquals(resourceService.resolve("res:Entity/target/metaborg/sdf.tbl"), syntaxFacet.parseTable);

        assertIterableEquals(syntaxFacet.startSymbols, "Start");

        final SemanticProviderFacet strategoFacet = impl.facet(SemanticProviderFacet.class);

        assertIterableEquals(strategoFacet.ctreeFiles, resourceService.resolve("res:Entity/target/metaborg/stratego.ctree"));
        assertIterableEquals(strategoFacet.jarFiles, resourceService.resolve("res:Entity/target/metaborg/stratego-javastrat.jar"));

        final AnalysisFacet analysisFacet = impl.facet(AnalysisFacet.class);

        assertEquals("editor-analyze", analysisFacet.strategyName);
    }
}
