package org.metaborg.core.tracing;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for reference resolution, resolving use sites to their definition sites.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 */
public interface IResolverService<P extends IParseUnit, A extends IAnalyzeUnit> {
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
     * @param input
     *            Parsed input to use for resolving and tracing.
     * 
     * @return Resolution if reference resolution was successful, or null if no resolution could be made.
     * @throws MetaborgException
     *             When reference resolution fails unexpectedly.
     */
    @Nullable Resolution resolve(int offset, P input) throws MetaborgException;

    /**
     * Attempt to resolve use site at {@code offset} in the source text, using given analysis result for resolving and
     * tracing.
     * 
     * @param offset
     *            Offset in the source text to perform reference resolution for.
     * @param input
     *            Analyzed input to use for resolving and tracing.
     * 
     * @return Resolution if reference resolution was successful, or null if no resolution could be made.
     * @throws MetaborgException
     *             When reference resolution fails unexpectedly.
     */
    @Nullable Resolution resolve(int offset, A input) throws MetaborgException;
}
