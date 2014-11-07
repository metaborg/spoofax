package org.metaborg.spoofax.core.service.syntax;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements ILanguageFacet {
    private final FileObject parseTable;
    private final Set<String> startSymbols;


    /**
     * Creates a syntax facet from a parse table provider and start symbols.
     * 
     * @param parseTableProvider
     *            Parse table provider.
     * @param startSymbols
     *            Set of start symbols.
     */
    public SyntaxFacet(FileObject parseTable, Set<String> startSymbols) {
        this.parseTable = parseTable;
        this.startSymbols = startSymbols;
    }


    /**
     * Returns the parse table provider.
     * 
     * @return Parse table provider.
     */
    public FileObject parseTable() {
        return parseTable;
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
