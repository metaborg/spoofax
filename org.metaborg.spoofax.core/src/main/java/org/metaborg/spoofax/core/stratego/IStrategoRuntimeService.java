package org.metaborg.spoofax.core.stratego;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.strategoxt.HybridInterpreter;

/**
 * Interface for a service that returns Stratego runtimes. Runtimes are created once and then cached, subsequent calls
 * are faster.
 */
public interface IStrategoRuntimeService extends ILanguageCache {
    /**
     * Returns a new Stratego runtime for given component, initialized with given context.
     * 
     * @param component
     *            Language component to load the Stratego CTree and JAR files from.
     * @param context
     *            Context to initialize the runtime with.
     * @return New Stratego runtime.
     * @throws MetaborgException
     *             When loading a Stratego CTree or JAR fails.
     */
    public abstract HybridInterpreter runtime(ILanguageComponent component, IContext context) throws MetaborgException;

    /**
     * @return Generic Stratego runtime, with just the standard libraries loaded.
     */
    public abstract HybridInterpreter genericRuntime();
}