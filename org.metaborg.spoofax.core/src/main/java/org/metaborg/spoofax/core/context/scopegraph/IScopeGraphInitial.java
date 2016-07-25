package org.metaborg.spoofax.core.context.scopegraph;

import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphInitial {

    public IStrategoTerm params();
    public IStrategoTerm constraint();
    
}
