package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ScopeGraphUnit implements IScopeGraphUnit {

    private final FileObject source;
    private final IStrategoTerm constraint;
    private final @Nullable IStrategoTerm analysis;

    public ScopeGraphUnit(FileObject source, IStrategoTerm constraint) {
        this(source, constraint, null);
    }

    public ScopeGraphUnit(FileObject source, IStrategoTerm constraint, IStrategoTerm analysis) {
        this.source = source;
        this.constraint = constraint;
        this.analysis = analysis;
    }


    @Override
    public FileObject source() {
        return source;
    }

    @Override
    public IStrategoTerm constraint() {
        return constraint;
    }
 
    @Override
    public IStrategoTerm analysis() {
        return analysis;
    }
    
}
