package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class ScopeGraphPrimitive extends AbstractPrimitive {

    public ScopeGraphPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    @Override public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
        throws InterpreterException {
        final Object contextObj = env.contextObject();
        if(!(contextObj instanceof IScopeGraphContext)) {
            throw new InterpreterException("Context does not implement IScopeGraphContext");
        }
        final IScopeGraphContext context = (IScopeGraphContext) env.contextObject();
        return call(context, env, svars, tvars);
    }

    public abstract boolean call(IScopeGraphContext context, IContext env,
            Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException;

}
