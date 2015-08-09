package org.metaborg.spoofax.core.stratego;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public class StrategoRuntimeUtils {
    /**
     * Invokes a Stratego strategy in given component.
     * 
     * @param service
     *            Stratego runtime service to get a runtime from.
     * @param component
     *            Component to invoke the strategy in.
     * @param context
     *            Context to initialize the runtime with.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When an error occurs getting a Stratego runtime.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public static @Nullable IStrategoTerm invoke(IStrategoRuntimeService service, ILanguageComponent component,
        IContext context, IStrategoTerm input, String strategy) throws MetaborgException {
        final HybridInterpreter runtime = service.runtime(component, context);
        return invoke(runtime, input, strategy);
    }

    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     * 
     * @param service
     *            Stratego runtime service to get a runtime from.
     * @param impl
     *            Language implementation to invoke the strategy in.
     * @param context
     *            Context to initialize the runtime with.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When an error occurs getting a Stratego runtime.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public static @Nullable IStrategoTerm invoke(IStrategoRuntimeService service, ILanguageImpl impl, IContext context,
        IStrategoTerm input, String strategy) throws MetaborgException {
        for(ILanguageComponent component : impl.components()) {
            if(component.facet(StrategoRuntimeFacet.class) == null) {
                continue;
            }

            final HybridInterpreter runtime = service.runtime(component, context);
            final IStrategoTerm result = invoke(runtime, input, strategy);
            if(result != null) {
                return result;
            }

        }
        return null;
    }

    /**
     * Invokes a Strategy strategy in given runtime.
     * 
     * @param runtime
     *            Stratego runtime to invoke the strategy in.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public static @Nullable IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy)
        throws MetaborgException {
        try {
            runtime.setCurrent(input);
            final boolean success = runtime.invoke(strategy);
            if(!success) {
                return null;
            }
            return runtime.current();
        } catch(InterpreterException e) {
            throw new MetaborgException("Invoking Stratego strategy failed unexpectedly", e);
        }
    }
}
