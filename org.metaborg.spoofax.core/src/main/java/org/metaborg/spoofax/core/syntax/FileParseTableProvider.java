package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.io.binary.TermReader;

public class FileParseTableProvider implements IParseTableProvider {
    private final FileObject resource;
    private final ITermFactory termFactory;

    private ParseTable parseTable;


    public FileParseTableProvider(FileObject resource, ITermFactory termFactory) {
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
            final IStrategoTerm parseTableTerm = termReader.parseFromStream(stream);
            
            FileObject grammar = resource.getParent().resolveFile("normgrammar.bin");
            if(grammar.exists()) {
                parseTable = new ParseTable(parseTableTerm, termFactory, grammar);
            } else {
                parseTable = new ParseTable(parseTableTerm, termFactory);
            }

        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }

        return parseTable;
    }
}
