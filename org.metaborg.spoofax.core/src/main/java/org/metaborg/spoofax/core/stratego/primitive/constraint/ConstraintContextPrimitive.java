package org.metaborg.spoofax.core.stratego.primitive.constraint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.metaborg.util.concurrent.IClosableLock;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public abstract class ConstraintContextPrimitive extends AbstractPrimitive {

    final protected int tvars;

    public ConstraintContextPrimitive(String name) {
        this(name, 0);
    }

    public ConstraintContextPrimitive(String name, int tvars) {
        super(name, 0, tvars);
        this.tvars = tvars;
    }

    @Override public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {
        final IConstraintContext context = constraintContext(env);
        final IStrategoTerm term = env.current();
        final List<IStrategoTerm> terms = Arrays.asList(tvars);
        final Optional<? extends IStrategoTerm> result;
        try(IClosableLock lock = context.read()) {
            result = call(context, term, terms, env.getFactory());
        }
        return result.map(t -> {
            env.setCurrent(t);
            return true;
        }).orElse(false);
    }

    protected abstract Optional<? extends IStrategoTerm> call(IConstraintContext context, IStrategoTerm term,
            List<IStrategoTerm> terms, ITermFactory factory) throws InterpreterException;

    private IConstraintContext constraintContext(IContext env) throws InterpreterException {
        final Object contextObj = env.contextObject();
        if(contextObj == null) {
            throw new InterpreterException("No context present.");
        }
        if(!(contextObj instanceof IConstraintContext)) {
            throw new InterpreterException("Context does not implement IConstraintContext");
        }
        final IConstraintContext context = (IConstraintContext) env.contextObject();
        return context;
    }

}