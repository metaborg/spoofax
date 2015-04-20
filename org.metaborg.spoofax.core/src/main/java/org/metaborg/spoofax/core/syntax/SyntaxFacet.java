package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.resource.ResourceService;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements ILanguageFacet {
    private static final long serialVersionUID = 2342326101518124130L;
    
	private transient FileObject parseTable;
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
    
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		ResourceService.writeFileObject(parseTable, out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		parseTable = ResourceService.readFileObject(in);
	}
}
