package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.nabl2.solution.INameResolution;
import org.metaborg.nabl2.solution.IScopeGraph;
import org.metaborg.solver.ISolution;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class FinalResult {

    public final IScopeGraph scopeGraph;
    public final INameResolution nameResolution;
    public final ISolution solution;
    public final IStrategoTerm errors;
    public final IStrategoTerm warnings;
    public final IStrategoTerm notes;
    public final IStrategoTerm analysis;

    public FinalResult(IScopeGraph scopeGraph, INameResolution nameResolution, ISolution solution, IStrategoTerm errors,
            IStrategoTerm warnings, IStrategoTerm notes, IStrategoTerm analysis) {
        this.errors = errors;
        this.warnings = warnings;
        this.notes = notes;
        this.scopeGraph = scopeGraph;
        this.nameResolution = nameResolution;
        this.solution = solution;
        this.analysis = analysis;
    }

}