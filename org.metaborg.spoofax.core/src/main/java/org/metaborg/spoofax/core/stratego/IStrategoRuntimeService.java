package org.metaborg.spoofax.core.stratego;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.context.IContext;
import org.strategoxt.HybridInterpreter;

/**
 * Service for the production of language-specific Stratego Interpreters. Precisely one interpreter per language is
 * cached; subsequent requests for new interpreters are based on the cached ones as prototypes.
 */
public interface IStrategoRuntimeService {
    /**
     * Obtain a new {@link HybridInterpreter} for given {@link IContext}. The produced interpreter is based on an
     * internally cached interpreter instance for the language in given context. If such a cache does not exist, then
     * this method first creates an internal cache for the language and then returns a new interpreter based on that
     * prototype. Note therefore that multiple calls to this method will return a different interpreter every time.
     * 
     * @param context
     *            Context to create the interpreter with.
     * @return A new interpreter for given language. All of the language's CTree and JAR files from
     *         {@link StrategoFacet#ctreeFiles()} and {@link StrategoFacet#jarFiles()} respectively are loaded into the
     *         interpreter.
     * @throws SpoofaxException
     *             When loading a CTree or JAR fails.
     */
    public abstract HybridInterpreter runtime(IContext context) throws SpoofaxException;

    public abstract HybridInterpreter genericRuntime();
}