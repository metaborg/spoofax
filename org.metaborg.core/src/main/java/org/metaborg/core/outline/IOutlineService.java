package org.metaborg.core.outline;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for creating outlines.
 * 
 * @param <P>
 *            Type of parsed fragments
 * @param <A>
 *            Type of analyzed fragments
 */
public interface IOutlineService<P, A> {
    /**
     * Checks if outlining is available for given language implementation.
     * 
     * @param language
     *            Language implementation to check.
     * @return True if outlining is available, false if not.
     */
    public abstract boolean available(ILanguageImpl language);

    /**
     * Creates an outline from given parse result.
     * 
     * @param result
     *            Parse result
     * 
     * @return Created outline, or null if the outline is empty.
     * @throws MetaborgException
     *             When creating an outline fails unexpectedly.
     */
    public abstract @Nullable IOutline outline(ParseResult<P> result) throws MetaborgException;

    /**
     * Creates an outline from given analysis result.
     * 
     * @param result
     *            Analysis result
     * 
     * @return Created outline, or null if the outline is empty.
     * @throws MetaborgException
     *             When creating an outline fails unexpectedly.
     */
    public abstract @Nullable IOutline outline(AnalysisFileResult<P, A> result) throws MetaborgException;
}
