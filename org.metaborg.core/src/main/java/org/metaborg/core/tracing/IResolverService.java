package org.metaborg.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for reference resolution, resolving use sites to their definition sites.
 * 
 * @param <P>
 *            Type of parsed fragments
 * @param <A>
 *            Type of analyzed fragments
 */
public interface IResolverService<P, A> {
    /**
     * Checks if reference resolution is available for given language implementation.
     * 
     * @param language
     *            Language implementation to check.
     * @return True if reference resolution is available, false if not.
     */
    boolean available(ILanguageImpl language);

    /**
     * Attempt to resolve use site at {@code offset} in the source text, using given parse result for resolving and
     * tracing.
     * 
     * @param offset
     *            Offset in the source text to perform reference resolution for.
     * @param result
     *            Parse result to use for resolving and tracing.
     * 
     * @return Resolution if reference resolution was successful, or null if no resolution could be made.
     * @throws MetaborgException
     *             When reference resolution fails unexpectedly.
     */
    @Nullable Resolution resolve(int offset, ParseResult<P> result) throws MetaborgException;

    /**
     * Attempt to resolve use site at {@code offset} in the source text, using given analysis result for resolving and
     * tracing.
     * 
     * @param offset
     *            Offset in the source text to perform reference resolution for.
     * @param result
     *            Analysis result to use for resolving and tracing.
     * 
     * @return Resolution if reference resolution was successful, or null if no resolution could be made.
     * @throws MetaborgException
     *             When reference resolution fails unexpectedly.
     */
    @Nullable Resolution resolve(int offset, AnalysisFileResult<P, A> result) throws MetaborgException;
}
