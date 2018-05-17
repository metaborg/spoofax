package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.spoofax.core.context.scopegraph.IMultiFileScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ISingleFileScopeGraphUnit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.nabl2.spoofax.analysis.IScopeGraphContext;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;
import mb.nabl2.spoofax.primitives.PrimitiveUtil;
import mb.nabl2.stratego.StrategoBlob;

@Deprecated
public class SG_set_resource_analysis extends AbstractPrimitive {

    public SG_set_resource_analysis() {
        super(SG_set_resource_analysis.class.getSimpleName(), 0, 0);
    }

    @Override public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {

        // get analysis
        final IStrategoTerm analysisTerm = env.current();
        final IScopeGraphUnit analysis = StrategoBlob.match(analysisTerm, IScopeGraphUnit.class)
                .orElseThrow(() -> new InterpreterException("Not a valid analysis term."));

        // get context and unit
        final IScopeGraphContext<?> context = PrimitiveUtil.scopeGraphContext(env);
        final IScopeGraphUnit unit = context.unit(analysis.resource());
        if(unit instanceof ISingleFileScopeGraphUnit) {
            ISingleFileScopeGraphUnit sfu = (ISingleFileScopeGraphUnit) unit;
            unit.solution().ifPresent(sfu::setSolution);
            unit.customSolution().ifPresent(sfu::setCustomSolution);
        } else if(unit instanceof IMultiFileScopeGraphUnit) {
            throw new InterpreterException("Can only set analysis for single-file units.");
        } else {
            throw new InterpreterException("Unknown unit type: " + unit.getClass());
        }

        return true;
    }

}