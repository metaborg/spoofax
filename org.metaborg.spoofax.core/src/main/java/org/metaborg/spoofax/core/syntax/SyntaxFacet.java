package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageFacet;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.util.iterators.Iterables2;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements ILanguageFacet {
    private static final long serialVersionUID = 2342326101518124130L;

    public transient FileObject parseTable;
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ResourceService.writeFileObject(parseTable, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        parseTable = ResourceService.readFileObject(in);
    }
}
