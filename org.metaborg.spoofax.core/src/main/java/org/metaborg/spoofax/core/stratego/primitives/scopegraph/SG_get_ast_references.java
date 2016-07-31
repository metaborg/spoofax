package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUtil;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_references extends ScopeGraphPrimitive {
    
    public SG_get_ast_references() {
        super(SG_get_ast_references.class.getSimpleName(), 0, 0);
    }


    @Override public boolean call(IScopeGraphContext context, IContext env,
            Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        ASTIndex astIndex;
        try {
            astIndex = ScopeGraphUtil.getASTIndex(env.current());
        } catch(MetaborgException ex) {
            throw new InterpreterException(ex);
        }
        IScopeGraphUnit unit = context.unit(astIndex.source);
        if(unit == null) {
            return false;
        }
        Collection<IStrategoTerm> values = unit.nameResolution(astIndex.index);
        if(values.isEmpty()) {
            return false;
        }
        env.setCurrent(env.getFactory().makeList(values));
        return true;
    }

}
