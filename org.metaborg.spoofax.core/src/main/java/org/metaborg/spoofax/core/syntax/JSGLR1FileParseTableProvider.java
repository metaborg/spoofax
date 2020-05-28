package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.io.binary.TermReader;

public class JSGLR1FileParseTableProvider implements IParseTableProvider {
    private final FileObject resource;
    private final ITermFactory termFactory;

    private ParseTable parseTable;

    public JSGLR1FileParseTableProvider(FileObject resource, ITermFactory termFactory) {
        this.resource = resource;
        this.termFactory = termFactory;
    }

    @Override public ParseTable parseTable() throws IOException {
        if(parseTable != null) {
            return parseTable;
        }

        resource.refresh();

        if(!resource.exists()) {
            throw new IOException("Could not load parse table from " + resource + ", file does not exist");
        }

        try(final InputStream stream = resource.getContent().getInputStream()) {
            final TermReader termReader = new TermReader(termFactory);
            IStrategoTerm parseTableTerm = termReader.parseFromStream(stream);

            // Name of parse table Java object is currently fixed as table.bin and table-completions.bin
            FileObject persistedTable;
            if(resource.getName().getBaseName().contains("-completions")) {
                persistedTable = resource.getParent().resolveFile("table-completions.bin");
            } else {
                persistedTable = resource.getParent().resolveFile("table.bin");
            }

            try {
                parseTable = new ParseTable(parseTableTerm, termFactory);
            } catch(Exception e) {
                // only read serialized table when table generation is dynamic (#states = 0)
                if(persistedTable.exists()
                    && e.getMessage() == "Parse table does not contain any state and normalized grammar is null") {
                    parseTable =
                        new ParseTable(parseTableTerm, termFactory, persistedTable, new ParseTableIO(persistedTable));
                } else {
                    throw e;
                }
            }
        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }

        return parseTable;
    }
}