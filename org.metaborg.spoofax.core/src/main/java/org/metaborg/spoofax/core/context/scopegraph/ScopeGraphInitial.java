package org.metaborg.spoofax.core.context.scopegraph;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ScopeGraphInitial implements IScopeGraphInitial {

    private final IStrategoTerm params;
    private final IStrategoTerm constraint;

    public ScopeGraphInitial(IStrategoTerm params, IStrategoTerm constraint) {
        this.params = params;
        this.constraint = constraint;
    }


    @Override
    public IStrategoTerm params() {
        return params;
    }

    @Override
    public IStrategoTerm constraint() {
        return constraint;
    }

}
