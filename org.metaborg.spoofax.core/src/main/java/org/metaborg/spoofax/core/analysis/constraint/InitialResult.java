package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.solver.constraints.IConstraint;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class InitialResult {

    public final IConstraint constraint;
    public final IStrategoTerm analysis;

    public InitialResult(IConstraint constraint, IStrategoTerm analysis) {
        this.analysis = analysis;
        this.constraint = constraint;
    }

}
