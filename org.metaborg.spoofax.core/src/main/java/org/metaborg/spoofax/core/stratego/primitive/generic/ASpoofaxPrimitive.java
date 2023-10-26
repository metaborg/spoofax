package org.metaborg.spoofax.core.stratego.primitive.generic;

import java.io.IOException;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * An improved primitive base class.
 */
public abstract class ASpoofaxPrimitive extends AbstractPrimitive {
    public ASpoofaxPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }


    protected abstract @Nullable IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException;


    @Override public boolean call(IContext context, Strategy[] svars, IStrategoTerm[] tvars)
        throws InterpreterException {
        final IStrategoTerm current = context.current();
        final ITermFactory factory = context.getFactory();
        try {
            final IStrategoTerm newCurrent = call(current, svars, tvars, factory, context);
            if(newCurrent != null) {
                context.setCurrent(newCurrent);
                return true;
            }
        } catch(MetaborgException | IOException e) {
            throw new InterpreterException("Executing primitive " + name + " failed unexpectedly", e);
        }
        return false;
    }


    protected @Nullable org.metaborg.core.context.IContext metaborgContext(IContext strategoContext) {
        return (org.metaborg.core.context.IContext) strategoContext.contextObject();
    }
}
