package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.MetaborgException;
import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.scopegraph.ScopeGraphException;
import org.metaborg.scopegraph.impl.NameResolution;
import org.metaborg.scopegraph.impl.ScopeGraph;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class FinalResult {

    public final IStrategoTerm errors;
    public final IStrategoTerm warnings;
    public final IStrategoTerm notes;
    public final IScopeGraph scopeGraph;
    public final INameResolution nameResolution;
    public final IStrategoTerm analysis;
 
    public FinalResult(IStrategoTerm errors, IStrategoTerm warnings,
            IStrategoTerm notes, IScopeGraph scopeGraph,
            INameResolution nameResolution, IStrategoTerm analysis) {
        this.errors = errors;
        this.warnings = warnings;
        this.notes = notes;
        this.scopeGraph = scopeGraph;
        this.nameResolution = nameResolution;
        this.analysis = analysis;
    }

    public static FinalResult fromTerm(IStrategoTerm term) throws MetaborgException {
        if(!(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl)term, "FinalResult", 4))) {
            throw new MetaborgException("Wrong format for final result.");
        }
        IStrategoTerm analysis = term.getSubterm(3);
        IScopeGraph scopeGraph = null;
        INameResolution nameResolution = null;
        for(IStrategoTerm component : analysis) {
            if(Tools.isTermAppl(component) && Tools.hasConstructor((IStrategoAppl)component, "ScopeGraph", 1)) {
                scopeGraph = new ScopeGraph(component.getSubterm(0));
            }
            if(Tools.isTermAppl(component) && Tools.hasConstructor((IStrategoAppl)component, "NameResolution", 1)) {
                try {
                    nameResolution = new NameResolution(component.getSubterm(0));
                } catch (ScopeGraphException e) {
                    throw new MetaborgException("Failed to construct name resolution.",e);
                }
            }
        }
        return new FinalResult(term.getSubterm(0), term.getSubterm(1),
                term.getSubterm(2), scopeGraph, nameResolution, analysis);
    }
 
}