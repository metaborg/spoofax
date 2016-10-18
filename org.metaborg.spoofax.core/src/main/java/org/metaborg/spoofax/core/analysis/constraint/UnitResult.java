package org.metaborg.spoofax.core.analysis.constraint;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnitResult {

    public final IStrategoTerm ast;
    public final IStrategoTerm analysis;

    public UnitResult(IStrategoTerm ast, IStrategoTerm analysis) {
        this.ast = ast;
        this.analysis = analysis;
    }

}