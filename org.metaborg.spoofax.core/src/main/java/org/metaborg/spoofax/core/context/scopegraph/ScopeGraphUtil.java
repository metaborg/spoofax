package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitives.scopegraph.ASTIndex;
import org.spoofax.interpreter.terms.IStrategoTerm;

public final class ScopeGraphUtil {

    private ScopeGraphUtil() {}
 
    public static ASTIndex getASTIndex(IStrategoTerm node) throws MetaborgException {
        for(IStrategoTerm anno : node.getAnnotations()) {
            if(ASTIndex.isASTIndex(anno)) {
                return ASTIndex.fromTerm(anno);
            }
        }
        throw new MetaborgException("Term has no AST index.");
    }

}
