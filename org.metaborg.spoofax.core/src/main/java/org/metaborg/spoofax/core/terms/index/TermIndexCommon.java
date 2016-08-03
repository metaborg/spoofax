package org.metaborg.spoofax.core.terms.index;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

public class TermIndexCommon {

    private TermIndexCommon() {}
    
    public static void index(final String resource, final IStrategoTerm term) {
        StrategoTermVisitee.topdown(new AStrategoTermVisitor() {
            private int currentId = 0;
            @Override public boolean visit(IStrategoTerm term) {
                TermIndex.put(term, resource, ++currentId);
                return true;
            }
        }, term);
    }

}
