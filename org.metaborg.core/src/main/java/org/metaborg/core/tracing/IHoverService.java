package org.metaborg.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for getting hover tooltip information.
 * 
 * @param <P>
 *            Type of parsed fragments
 * @param <A>
 *            Type of analyzed fragments
 */
public interface IHoverService<P, A> {
    /**
     * Checks if hover tooltip information is available for given language implementation.
     * 
     * @param language
     *            Language implementation to check.
     * @return True if hover tooltip information is available, false if not.
     */
    boolean available(ILanguageImpl language);

    /**
     * Attempts to get hover tooltip information at {@code offset} in the source text, using given parse result for
     * resolving and tracing.
     * 
     * @param offset
     *            Offset in the source text.
     * @param result
     *            Parse result to use for tracing.
     * 
     * @return Hover tooltip information if successful, or null if no information can be retrieved.
     * @throws MetaborgException
     *             When retrieving information fails unexpectedly.
     */
    @Nullable Hover hover(int offset, ParseResult<P> result) throws MetaborgException;

    /**
     * Attempts to get hover tooltip information at {@code offset} in the source text, using given analysis result for
     * resolving and tracing.
     * 
     * @param offset
     *            Offset in the source text.
     * @param result
     *            Analysis result to use for tracing.
     * 
     * @return Hover tooltip information if successful, or null if no information can be retrieved.
     * @throws MetaborgException
     *             When retrieving information fails unexpectedly.
     */
    @Nullable Hover hover(int offset, AnalysisFileResult<P, A> result) throws MetaborgException;
}
