package org.metaborg.core.style;

import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for styling of categorized parse and analysis results.
 * 
 * @param <F>
 *            Type of fragments.
 */
public interface IStylerService<F> {
    /**
     * Returns a styling of given categorized parse result.
     * 
     * @param langImpl
     *            Language implementation that contains the styling logic.
     * @param categorization
     *            Parse result categorization to style.
     * @return Iterable over styles assigned to regions of the source text. Regions do not overlap and are iterated over
     *         in ascending order.
     */
    Iterable<IRegionStyle<F>> styleParsed(ILanguageImpl langImpl, Iterable<IRegionCategory<F>> categorization);

    /**
     * Returns a styling of given categorized analysis result.
     * 
     * @param langImpl
     *            Language implementation that contains the styling logic.
     * @param categorization
     *            Analysis result categorization to style.
     * @return Iterable over styles assigned to regions of the source text. Regions do not overlap and are iterated over
     *         in ascending order.
     */
    Iterable<IRegionStyle<F>> styleAnalyzed(ILanguageImpl langImpl, Iterable<IRegionCategory<F>> categorization);
}
