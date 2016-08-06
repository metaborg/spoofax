package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class SG_get_analysis extends ScopeGraphPrimitive {
 
    @Inject public SG_get_analysis() {
        super(SG_get_analysis.class.getSimpleName(), 0, 0);
    } 


    @Override public boolean call(IScopeGraphContext context, IContext env,
            Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        String source = Tools.asJavaString(env.current());
        IScopeGraphUnit unit = context.unit(source);
        if(unit == null) {
            return false;
        }
        IStrategoTerm analysis = unit.result();
        if(analysis == null) {
            return false;
        }
        env.setCurrent(analysis);
        return true;
    }

}