package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.language.ILanguage;

/**
 * Interface for styling of categorized parse and analysis results.
 * 
 * @param <ParseT>
 *            Type of the parsed fragments.
 * @param <AnalysisT>
 *            Type of the analyzed fragments.
 */
public interface IStylerService<ParseT, AnalysisT> {
    /**
     * Returns a styling of given categorized parse result.
     * 
     * @param language
     *            Language that contains the styling logic.
     * @param categorization
     *            Parse result categorization to style.
     * @return Iterable over styles assigned to regions of the source text. Regions do not overlap
     *         and are iterated over in ascending order.
     */
    public abstract Iterable<IRegionStyle<ParseT>> styleParsed(ILanguage language,
        Iterable<IRegionCategory<ParseT>> categorization);

    /**
     * Returns a styling of given categorized analysis result.
     * 
     * @param language
     *            Language that contains the styling logic.
     * @param categorization
     *            Analysis result categorization to style.
     * @return Iterable over styles assigned to regions of the source text. Regions do not overlap
     *         and are iterated over in ascending order.
     */
    public abstract Iterable<IRegionStyle<AnalysisT>> styleAnalyzed(ILanguage language,
        Iterable<IRegionCategory<AnalysisT>> categorization);
}
