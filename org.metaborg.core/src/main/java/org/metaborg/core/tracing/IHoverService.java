package org.metaborg.core.tracing;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for getting hover tooltip information.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 */
public interface IHoverService<P extends IParseUnit, A extends IAnalyzeUnit> {
    /**
     * Checks if hover tooltip information is available for given language implementation.
     * 
     * @param language
     *            Language implementation to check.
     * @return True if hover tooltip information is available, false if not.
     */
    boolean available(ILanguageImpl language);

    /**
     * Attempts to get hover tooltip information at {@code offset} in the source text, using given parsed input for
     * resolving and tracing.
     * 
     * @param offset
     *            Offset in the source text.
     * @param input
     *            Parsed input to use for tracing.
     * 
     * @return Hover tooltip information if successful, or null if no information can be retrieved.
     * @throws MetaborgException
     *             When retrieving information fails unexpectedly.
     */
    @Nullable Hover hover(int offset, P input) throws MetaborgException;

    /**
     * Attempts to get hover tooltip information at {@code offset} in the source text, using given analyzed input for
     * resolving and tracing.
     * 
     * @param offset
     *            Offset in the source text.
     * @param input
     *            Analyzed input to use for tracing.
     * 
     * @return Hover tooltip information if successful, or null if no information can be retrieved.
     * @throws MetaborgException
     *             When retrieving information fails unexpectedly.
     */
    @Nullable Hover hover(int offset, A input) throws MetaborgException;
}
