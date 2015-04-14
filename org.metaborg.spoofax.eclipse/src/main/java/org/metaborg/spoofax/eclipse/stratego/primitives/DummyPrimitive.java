package org.metaborg.spoofax.eclipse.stratego.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class DummyPrimitive extends AbstractPrimitive {
    public DummyPrimitive(String name, int strategyArity, int termArity) {
        super(name, strategyArity, termArity);
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        return true;
    }
}
