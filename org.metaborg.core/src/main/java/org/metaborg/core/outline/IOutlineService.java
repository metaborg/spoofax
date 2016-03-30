package org.metaborg.core.outline;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for creating outlines.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 */
public interface IOutlineService<P extends IParseUnit, A extends IAnalyzeUnit> {
    /**
     * Checks if outlining is available for given language implementation.
     * 
     * @param langImpl
     *            Language implementation to check.
     * 
     * @return True if outlining is available, false if not.
     */
    boolean available(ILanguageImpl langImpl);

    /**
     * Creates an outline from given parse unit.
     * 
     * @param result
     *            Parsed result.
     * 
     * @return Created outline, or null if the outline is empty.
     * @throws MetaborgException
     *             When creating an outline fails unexpectedly.
     */
    @Nullable IOutline outline(P result) throws MetaborgException;

    /**
     * Creates an outline from given analyze unit.
     * 
     * @param result
     *            Analyzed result.
     * 
     * @return Created outline, or null if the outline is empty.
     * @throws MetaborgException
     *             When creating an outline fails unexpectedly.
     */
    @Nullable IOutline outline(A result) throws MetaborgException;
}
