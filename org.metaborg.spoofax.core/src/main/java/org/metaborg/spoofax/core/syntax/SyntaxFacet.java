package org.metaborg.spoofax.core.syntax;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.util.iterators.Iterables2;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements ILanguageFacet {
    public final FileObject parseTable;
    public final Iterable<String> startSymbols;
    public final Iterable<String> singleLineCommentPrefixes;
    public final Iterable<MultiLineCommentCharacters> multiLineCommentCharacters;
    public final Iterable<FenceCharacters> fenceCharacters;


    /**
     * Creates a syntax facet from a parse table provider and start symbols.
     * 
     * @param parseTableProvider
     *            Parse table provider.
     * @param startSymbols
     *            Set of start symbols.
     */
    public SyntaxFacet(FileObject parseTable, Iterable<String> startSymbols) {
        this(parseTable, startSymbols, Iterables2.<String>empty(), Iterables2.<MultiLineCommentCharacters>empty(),
            Iterables2.<FenceCharacters>empty());
    }

    /**
     * Creates a syntax facet from syntax configuration.
     * 
     * @param parseTableProvider
     *            Parse table provider.
     * @param startSymbols
     *            Set of start symbols.
     * @param singleLineCommentPrefixes
     *            Single line comment prefixes.
     * @param multiLineCommentCharacters
     *            Multi line comment characters.
     * @param fenceCharacters
     *            Fence characters.
     */
    public SyntaxFacet(FileObject parseTable, Iterable<String> startSymbols,
        Iterable<String> singleLineCommentPrefixes, Iterable<MultiLineCommentCharacters> multiLineCommentCharacters,
        Iterable<FenceCharacters> fenceCharacters) {
        this.parseTable = parseTable;
        this.startSymbols = startSymbols;
        this.singleLineCommentPrefixes = singleLineCommentPrefixes;
        this.multiLineCommentCharacters = multiLineCommentCharacters;
        this.fenceCharacters = fenceCharacters;
    }
}
