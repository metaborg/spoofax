package org.metaborg.spoofax.core.stratego;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public class StrategoRuntimeUtils {
    public static @Nullable IStrategoTerm invoke(HybridInterpreter interpreter, IStrategoTerm input, String function)
        throws SpoofaxRuntimeException {
        try {
            interpreter.setCurrent(input);
            final boolean success = interpreter.invoke(function);
            if(!success) {
                return null;
            }
            return interpreter.current();
        } catch(InterpreterException e) {
            throw new SpoofaxRuntimeException("Invoking Stratego function failed", e);
        }
    }
}
