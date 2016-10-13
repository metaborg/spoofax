package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import java.util.Collection;

import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.metaborg.scopegraph.indices.ITermIndex;
import org.metaborg.scopegraph.indices.TermIndex;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_references extends ScopeGraphPrimitive {

    private final static ILogger logger = LoggerUtils.logger(SG_get_ast_references.class);

    public SG_get_ast_references() {
        super(SG_get_ast_references.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] strategies,
            IStrategoTerm[] terms) throws InterpreterException {
        final ITermIndex index = TermIndex.get(env.current());
        if (index == null) {
            logger.warn("get-ast-references called with non-AST argument {}", env.current());
            return false;
        }
        IScopeGraphUnit unit = context.unit(index.resource());
        if (unit == null) {
            return false;
        }
        INameResolution nameResolution = unit.nameResolution();
        if (nameResolution == null) {
            return false;
        }
        Collection<IStrategoTerm> indices = nameResolution.astPaths(index);
        env.setCurrent(env.getFactory().makeList(indices));
        return true;
    }

}