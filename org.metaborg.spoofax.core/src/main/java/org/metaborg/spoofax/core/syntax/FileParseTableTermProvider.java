package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;

public class FileParseTableTermProvider implements IParseTableTermProvider {
    private final FileObject resource;
    private final ITermFactory termFactory;

    private IStrategoTerm parseTableTerm;

    public FileParseTableTermProvider(FileObject resource, ITermFactory termFactory) {
        this.resource = resource;
        this.termFactory = termFactory;
    }
    
    @Override public FileObject resource() {
        return resource;
    }

    @Override public IStrategoTerm parseTableTerm() throws IOException {
        if(parseTableTerm != null) {
            return parseTableTerm;
        }

        resource.refresh();
        
        if(!resource.exists()) {
            throw new IOException("Could not load parse table from " + resource + ", file does not exist");
        }

        try(final InputStream stream = resource.getContent().getInputStream()) {
            final TermReader termReader = new TermReader(termFactory);
            
            parseTableTerm = termReader.parseFromStream(stream);
        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }

        return parseTableTerm;
    }
}
