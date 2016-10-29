package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import org.metaborg.scopegraph.ScopeGraphException;
import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.indices.TermIndexCommon;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_index_sublist extends ScopeGraphPrimitive {

    public SG_index_sublist() {
        super(SG_index_sublist.class.getSimpleName(), 0, 1);
    }

    @Override public boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] strategies,
            IStrategoTerm[] terms) throws InterpreterException {
        try {
            TermIndexCommon.indexSublist(terms[0], env.current());
        } catch (ScopeGraphException e) {
            throw new InterpreterException(e);
        }
        return true;
    }

}
