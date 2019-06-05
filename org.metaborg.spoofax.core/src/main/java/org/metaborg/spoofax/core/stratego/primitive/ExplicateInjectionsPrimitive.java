package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.util.functions.Function1;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.Injections;

public class ExplicateInjectionsPrimitive extends AbstractPrimitive {

    public ExplicateInjectionsPrimitive() {
        super("SSL_EXT_explicate_injections", 1, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        final Strategy smangle = svars[0];
        final Function1<String, String> mangle = (name) -> {
            final IStrategoTerm originalTerm = env.current();
            try {
                env.setCurrent(env.getFactory().makeString(name));
                if(smangle.evaluate(env)) {
                    name = Tools.asJavaString(env.current());
                }
                return name;
            } catch(InterpreterException ex) {
                throw new InterpreterRuntimeException(ex);
            } finally {
                env.setCurrent(originalTerm);
            }
        };
        final Injections injections = new Injections(env.getFactory(), mangle);
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