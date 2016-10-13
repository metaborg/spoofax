package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.metaborg.scopegraph.indices.TermIndex;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_analysis extends ScopeGraphPrimitive {

    private static final ILogger logger = LoggerUtils.logger(SG_get_ast_analysis.class);

    public SG_get_ast_analysis() {
        super(SG_get_ast_analysis.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] strategies,
            IStrategoTerm[] terms) throws InterpreterException {
        final TermIndex index = TermIndex.get(env.current());
        if (index == null) {
            logger.warn("get-analysis called with non-AST argument {}", env.current());
            return false;
        }
        final IScopeGraphUnit unit = context.unit(index.resource());
        final IStrategoTerm analysis = unit.analysis();
        if (analysis == null) {
            return false;
        }
        env.setCurrent(analysis);
        return true;
    }

}