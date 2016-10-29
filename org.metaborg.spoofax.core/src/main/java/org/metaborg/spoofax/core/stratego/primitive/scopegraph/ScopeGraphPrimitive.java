package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ISpoofaxScopeGraphContext;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class ScopeGraphPrimitive extends AbstractPrimitive {

    private static ILogger logger = LoggerUtils.logger(ScopeGraphPrimitive.class);

    public ScopeGraphPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    @Override public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {
        final Object contextObj = env.contextObject();
        if (contextObj == null) {
            logger.warn("Context is null.");
            return false;
        }
        if (!(contextObj instanceof IScopeGraphContext)) {
            throw new InterpreterException("Context does not implement IScopeGraphContext");
        }
        final ISpoofaxScopeGraphContext<?> context = (ISpoofaxScopeGraphContext<?>) env.contextObject();
        try(IClosableLock lock = context.read()) {
            return call(context, env, svars, tvars);
        }
    }

    public abstract boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException;

}
