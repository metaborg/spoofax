package org.metaborg.spoofax.core.stratego.primitive.scopegraph;

import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class SG_get_resource_partial_analysis extends ScopeGraphPrimitive {

    @Inject public SG_get_resource_partial_analysis() {
        super(SG_get_resource_partial_analysis.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IScopeGraphContext<?> context, IContext env, Strategy[] strategies,
            IStrategoTerm[] terms) throws InterpreterException {
        if (!Tools.isTermString(env.current())) {
            throw new InterpreterException("Expected string argument.");
        }
        final String resource = Tools.asJavaString(env.current());
        final IScopeGraphUnit unit = context.unit(resource);
        final IStrategoTerm analysis = unit.partialAnalysis();
        if (analysis == null) {
            return false;
        }
        env.setCurrent(analysis);
        return true;
    }

}