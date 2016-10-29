package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ProjectPathPrimitive extends ASpoofaxContextPrimitive {
    @Inject public ProjectPathPrimitive() {
        super("project_path", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        return factory.makeString(context.location().getName().getURI());
    }
}
