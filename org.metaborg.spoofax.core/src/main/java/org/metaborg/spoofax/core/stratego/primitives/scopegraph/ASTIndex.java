package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class ASTIndex {

    public final String source;
    public final IStrategoTerm index;
 
    public ASTIndex(String source, IStrategoTerm index) {
        this.source = source;
        this.index = index;
    }

    public IStrategoTerm toTerm(ITermFactory termFactory) {
        IStrategoConstructor ctor =
                termFactory.makeConstructor(ASTIndex.class.getSimpleName(), 2);
        return termFactory.makeAppl(ctor, termFactory.makeString(source), index);
    }
 
    public static boolean isASTIndex(IStrategoTerm term) {
        return term.getTermType() == IStrategoTerm.APPL &&
                Tools.hasConstructor((IStrategoAppl)term, ASTIndex.class.getSimpleName(), 2);
    }
    
    public static ASTIndex fromTerm(IStrategoTerm term) {
        if(!isASTIndex(term)) {
            throw new IllegalArgumentException("Term is not an ASTIndex.");
        }
        String source = Tools.asJavaString(term.getSubterm(0));
        IStrategoTerm index = term.getSubterm(1);
        return new ASTIndex(source, index);
    }

}
