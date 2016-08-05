package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.MetaborgException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class FinalResult {

    public final IStrategoTerm errors;
    public final IStrategoTerm warnings;
    public final IStrategoTerm notes;
    public final IStrategoTerm solution;
    
    public FinalResult(IStrategoTerm errors, IStrategoTerm warnings,
            IStrategoTerm notes, IStrategoTerm solution) {
        this.errors = errors;
        this.warnings = warnings;
        this.notes = notes;
        this.solution = solution;
    }

    public static FinalResult fromTerm(IStrategoTerm term) throws MetaborgException {
        if(!Tools.hasConstructor((IStrategoAppl)term, "FinalResult", 4)) {
            throw new MetaborgException("Wrong format for final result.");
        }
        return new FinalResult(term.getSubterm(0), term.getSubterm(1),
                term.getSubterm(2), term.getSubterm(3));
    }
    
}