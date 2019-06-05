package org.metaborg.spoofax.core.stratego.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.Injections;

public class ExplicateInjectionsPrimitive extends AbstractPrimitive {

    public ExplicateInjectionsPrimitive() {
        super("SSL_EXT_explicate_injections", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        final Injections injections = new Injections(env.getFactory());
        final IStrategoTerm result = injections.explicate(env.current());
        env.setCurrent(result);
        return true;
    }

}