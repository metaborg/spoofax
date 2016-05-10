package org.metaborg.spoofax.core.stratego.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ProjectPathPrimitive extends AbstractPrimitive {
    @Inject public ProjectPathPrimitive() {
        super("SSL_EXT_projectpath", 0, 0);
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final ITermFactory factory = env.getFactory();
        final org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();
        final IStrategoTerm pathTerm = factory.makeString(context.location().getName().getURI());
        env.setCurrent(pathTerm);
        return true;
    }
}
