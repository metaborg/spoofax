package org.metaborg.spoofax.core.stratego.primitive.generic;

import java.io.IOException;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * A primitive base class that requires a Spoofax context to be available.
 */
public abstract class ASpoofaxContextPrimitive extends ASpoofaxPrimitive {
    public ASpoofaxContextPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }


    protected abstract @Nullable IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException;


    protected @Nullable IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, org.spoofax.interpreter.core.IContext strategoContext)
        throws MetaborgException, IOException {
        final IContext context = metaborgContext(strategoContext);
        if(context == null) {
            throw new MetaborgException("Cannot execute primitive " + name + ", no Spoofax context was set");
        }
        return call(current, svars, tvars, factory, context);
    }
}
