package org.metaborg.spoofax.core.context.scopegraph;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ScopeGraphUnit implements IScopeGraphUnit {

    private final FileObject source;
    private final IStrategoTerm constraint;

    public ScopeGraphUnit(FileObject source, IStrategoTerm constraint) {
        this.source = source;
        this.constraint = constraint;
    }


    @Override
    public FileObject source() {
        return source;
    }

    @Override
    public IStrategoTerm constraint() {
        return constraint;
    }
 
}
