package org.metaborg.spoofax.core.stratego;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

/**
 * Service for the production of language-specific Stratego Interpreters. Precisely one interpreter per
 * language is cached; subsequent requests for new interpreters are based on the cached ones as prototypes.
 */
public interface IStrategoRuntimeService {

    /**
     * Obtain a new {@link HybridInterpreter} for the given {@link ALanguage}. The produced interpreter is
     * based on an internally cached interpreter instance for the given language. If such a cache does not
     * exist, then this method first creates an internal cache for the language and then returns a new
     * interpreter based on that prototype. Note therefore that multiple calls to this method will return a
     * different interpreter every time.
     * 
     * @param lang
     *            The language for which to create a new interpreter.
     * @return A new interpret for the given language. All of the language's files (
     *         {@link ALanguage#getCompilerFiles()} are loaded into the interpreter.
     */
    public abstract HybridInterpreter getRuntime(ILanguage lang) throws SpoofaxException;

    public abstract @Nullable IStrategoTerm callStratego(ILanguage lang, String strategy,
        IStrategoTerm input, @Nullable FileObject workingLocation) throws SpoofaxException;
}