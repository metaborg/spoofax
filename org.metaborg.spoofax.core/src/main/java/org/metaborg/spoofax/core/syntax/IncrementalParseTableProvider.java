package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.io.binary.TermReader;

public class IncrementalParseTableProvider implements IParseTableProvider {
    private final FileObject resource;
    private final ITermFactory termFactory;
    private final org.metaborg.sdf2table.parsetable.ParseTable referenceTable;

    private ParseTable parseTable;


    public IncrementalParseTableProvider(FileObject resource, ITermFactory termFactory,
        org.metaborg.sdf2table.parsetable.ParseTable referenceTable) {
        this.resource = resource;
        this.termFactory = termFactory;
        this.referenceTable = referenceTable;
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
            final IStrategoTerm parseTableTerm = termReader.parseFromStream(stream);

            FileObject persistedTable = resource.getParent().resolveFile("table.bin");
            if(persistedTable.exists()) {
                if(referenceTable != null) {
                    parseTable = new ParseTable(parseTableTerm, termFactory, persistedTable, referenceTable);
                } else {
                    parseTable = new ParseTable(parseTableTerm, termFactory, persistedTable);
                }
            } else {
                parseTable = new ParseTable(parseTableTerm, termFactory);
            }

        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }

        return parseTable;
    }
}
