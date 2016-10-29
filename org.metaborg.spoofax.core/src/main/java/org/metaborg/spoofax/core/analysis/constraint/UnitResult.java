package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.MetaborgException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnitResult {

    public final IStrategoTerm ast;
    public final IStrategoTerm solution;
    
    public UnitResult(IStrategoTerm ast, IStrategoTerm solution) {
        this.ast = ast;
        this.solution = solution;
    }

    public static UnitResult fromTerm(IStrategoTerm term) throws MetaborgException {
        if(!Tools.hasConstructor((IStrategoAppl)term, "UnitResult", 2)) {
            throw new MetaborgException("Wrong format for unit result.");
        }
        return new UnitResult(term.getSubterm(0), term.getSubterm(1));
    }
    
}