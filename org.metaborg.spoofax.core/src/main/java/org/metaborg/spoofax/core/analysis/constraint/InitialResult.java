package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.MetaborgException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class InitialResult {

    public final IStrategoTerm solution;
    
    public InitialResult(IStrategoTerm solution) {
        this.solution = solution;
    }

    public static InitialResult fromTerm(IStrategoTerm term) throws MetaborgException {
        if(!Tools.hasConstructor((IStrategoAppl)term, "InitialResult", 1)) {
            throw new MetaborgException("Wrong format for initial result.");
        }
        return new InitialResult(term.getSubterm(0));
    }
    
}
