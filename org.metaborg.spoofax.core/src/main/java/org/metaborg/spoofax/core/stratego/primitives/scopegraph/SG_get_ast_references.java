package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import java.util.Collection;

import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.terms.index.ITermIndex;
import org.metaborg.spoofax.core.terms.index.TermIndex;
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
        ITermIndex index = TermIndex.get(env.current());
        if(index == null) {
            throw new InterpreterException("Term has no AST index.");
        }
        IScopeGraphUnit unit = context.unit(index.resource());
        if(unit == null) {
            return false;
        }
        Collection<IStrategoTerm> values = unit.nameResolution(index.nodeId());
        if(values.isEmpty()) {
            return false;
        }
        env.setCurrent(env.getFactory().makeList(values));
        return true;
    }

}
