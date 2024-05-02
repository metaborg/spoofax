package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.metaborg.parsetable.ParseTableReader;
import org.spoofax.terms.io.binary.TermReader;

public class JSGLR2FileParseTableProvider implements IParseTableProvider {
    private final FileObject resource;
    private final ITermFactory termFactory;

    private IParseTable parseTable;

    public JSGLR2FileParseTableProvider(FileObject resource, ITermFactory termFactory) {
        this.resource = resource;
        this.termFactory = termFactory;
    }

    @Override public IParseTable parseTable() throws IOException {
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

            FileObject persistedTable = resource.getParent().resolveFile("table.bin");
            parseTable = new ParseTableReader().read(parseTableTerm);

            // only read serialized table when table generation is dynamic (#states = 0)
            // or when using layout-sensitive parsing
            if((parseTable.totalStates() == 0 || parseTable.isLayoutSensitive()) && persistedTable.exists()) {
                ParseTableIO ptg = new ParseTableIO(persistedTable.getContent().getInputStream(), true);

                org.metaborg.sdf2table.parsetable.ParseTable parseTableFromSerializable = ptg.getParseTable();

                // TODO: markRejectableStates(states);

                return parseTableFromSerializable;
            }

        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }

        return parseTable;
    }
}
