package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.MetaborgException;
import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.scopegraph.impl.ASTMetadata;
import org.metaborg.scopegraph.impl.NameResolution;
import org.metaborg.scopegraph.impl.OccurrenceTypes;
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
    public final OccurrenceTypes occurrenceTypes;
    public final ASTMetadata astMetadata;
    public final IStrategoTerm analysis;

    public FinalResult(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, IScopeGraph scopeGraph,
            INameResolution nameResolution, OccurrenceTypes occurrenceTypes, ASTMetadata astMetadata,
            IStrategoTerm analysis) {
        this.errors = errors;
        this.warnings = warnings;
        this.notes = notes;
        this.scopeGraph = scopeGraph;
        this.nameResolution = nameResolution;
        this.occurrenceTypes = occurrenceTypes;
        this.astMetadata = astMetadata;
        this.analysis = analysis;
    }

    public static FinalResult fromTerm(IStrategoTerm term) throws MetaborgException {
        if (!(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl) term, "FinalResult", 4))) {
            throw new MetaborgException("Wrong format for final result.");
        }
        IStrategoTerm analysis = term.getSubterm(3);
        IScopeGraph scopeGraph = null;
        INameResolution nameResolution = null;
        OccurrenceTypes occurrenceTypes = null;
        ASTMetadata astMetadata = null;
        for (IStrategoTerm component : analysis) {
            if (ScopeGraph.is(component)) {
                scopeGraph = ScopeGraph.of(component);
            }
            if (NameResolution.is(component)) {
                nameResolution = NameResolution.of(component);
            }
            if (OccurrenceTypes.is(component)) {
                occurrenceTypes = OccurrenceTypes.of(component);
            }
            if (ASTMetadata.is(component)) {
                astMetadata = ASTMetadata.of(component);
            }
        }
        return new FinalResult(term.getSubterm(0), term.getSubterm(1), term.getSubterm(2), scopeGraph, nameResolution,
                occurrenceTypes, astMetadata, analysis);
    }

}