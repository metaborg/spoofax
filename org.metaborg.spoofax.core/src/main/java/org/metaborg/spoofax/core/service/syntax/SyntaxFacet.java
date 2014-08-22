package org.metaborg.spoofax.core.service.syntax;

import java.util.Set;

import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.parser.IParseTableProvider;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements ILanguageFacet {
    private final IParseTableProvider parseTableProvider;
    private final Set<String> startSymbols;


    /**
     * Creates a syntax facet from a parse table provider and start symbols.
     * 
     * @param parseTableProvider
     *            Parse table provider.
     * @param startSymbols
     *            Set of start symbols.
     */
    public SyntaxFacet(IParseTableProvider parseTableProvider, Set<String> startSymbols) {
        this.parseTableProvider = parseTableProvider;
        this.startSymbols = startSymbols;
    }


    /**
     * Returns the parse table provider.
     * 
     * @return Parse table provider.
     */
    public IParseTableProvider parseTableProvider() {
        return parseTableProvider;
    }

    /**
     * Returns the start symbols.
     * 
     * @return Iterable over the start symbols.
     */
    public Iterable<String> startSymbols() {
        return startSymbols;
    }
}
