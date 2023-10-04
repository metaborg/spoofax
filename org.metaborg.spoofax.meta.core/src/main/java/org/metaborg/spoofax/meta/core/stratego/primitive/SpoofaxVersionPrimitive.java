package org.metaborg.spoofax.meta.core.stratego.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class SpoofaxVersionPrimitive extends AbstractPrimitive {
    @Inject public SpoofaxVersionPrimitive() {
        super("spoofax_version", 0, 0);
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        env.setCurrent(env.getFactory().makeInt(2));
        return true;
    }
}
