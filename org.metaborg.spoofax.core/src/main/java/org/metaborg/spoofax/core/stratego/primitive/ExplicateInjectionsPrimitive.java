package org.metaborg.spoofax.core.stratego.primitive;

import java.util.Optional;

import org.metaborg.util.functions.PartialFunction2;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.jsglr.client.imploder.Injections;

public class ExplicateInjectionsPrimitive extends AbstractPrimitive {

    public ExplicateInjectionsPrimitive() {
        super("SSL_EXT_explicate_injections", 1, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        final Strategy sInjName = svars[0];
        final PartialFunction2<String, String, String> injName = (sort, intoSort) -> {
            final IStrategoTerm originalTerm = env.current();
            final IStrategoString sortTerm = env.getFactory().makeString(sort);
            final IStrategoString intoSortTerm = env.getFactory().makeString(intoSort);
            final IStrategoTuple input = env.getFactory().makeTuple(sortTerm, intoSortTerm);
            try {
                env.setCurrent(input);
                if(sInjName.evaluate(env)) {
                    return Optional.of(Tools.asJavaString(env.current()));
                } else {
                    return Optional.empty();
                }
            } catch(InterpreterException ex) {
                throw new InterpreterRuntimeException(ex);
            } finally {
                env.setCurrent(originalTerm);
            }
        };
        final Injections injections = new Injections(env.getFactory(), injName);
        final IStrategoTerm result;
        try {
            result = injections.explicate(env.current());
        } catch(InterpreterRuntimeException ex) {
            throw ex.getCause();
        }
        env.setCurrent(result);
        return true;
    }

    private static class InterpreterRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public final InterpreterException cause;

        public InterpreterRuntimeException(InterpreterException cause) {
            super(cause);
            this.cause = cause;
        }

        @Override public synchronized InterpreterException getCause() {
            return cause;
        }

    }

}