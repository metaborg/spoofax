package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.indices.TermIndex;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_index extends ScopeGraphPrimitive {

    public SG_get_ast_index() {
        super(SG_get_ast_index.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] strategies,
            IStrategoTerm[] terms) throws InterpreterException {
        TermIndex index = TermIndex.get(env.current());
        if (index == null) {
            return false;
        }
        env.setCurrent(index.toTerm(env.getFactory()));
        return true;
    }

}
