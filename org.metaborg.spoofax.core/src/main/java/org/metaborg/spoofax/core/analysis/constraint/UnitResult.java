package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.solver.constraints.IConstraint;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnitResult {

    public final IStrategoTerm ast;
    public final IConstraint constraint;
    public final IStrategoTerm analysis;

    public UnitResult(IStrategoTerm ast, IConstraint constraint, IStrategoTerm analysis) {
        this.ast = ast;
        this.analysis = analysis;
        this.constraint = constraint;
    }

}