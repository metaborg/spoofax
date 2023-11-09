package org.metaborg.spoofax.meta.core.stratego.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;


public class SpoofaxVersionPrimitive extends AbstractPrimitive {
    @jakarta.inject.Inject @javax.inject.Inject public SpoofaxVersionPrimitive() {
        super("spoofax_version", 0, 0);
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        env.setCurrent(env.getFactory().makeInt(2));
        return true;
    }
}
