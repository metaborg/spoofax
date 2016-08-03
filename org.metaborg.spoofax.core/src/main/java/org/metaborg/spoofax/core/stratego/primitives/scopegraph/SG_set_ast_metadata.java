package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.terms.index.ITermIndex;
import org.metaborg.spoofax.core.terms.index.TermIndex;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_set_ast_metadata extends ScopeGraphPrimitive {
    
    public SG_set_ast_metadata() {
        super(SG_set_ast_metadata.class.getSimpleName(), 0, 2);
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
            throw new InterpreterException("Cannot set metadata for a resource that is not being analyzed.");
        }
        unit.setMetadata(index.nodeId(), terms[0], terms[1]);
        return true;
    }

}