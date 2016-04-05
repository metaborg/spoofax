package org.metaborg.core.style;

import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for categorization of parse and analysis results.
 * 
 * @param <P>
 *            Type of parse units
 * @param <A>
 *            Type of analyze units.
 * @param <F>
 *            Type of fragments.
 */
public interface ICategorizerService<P extends IParseUnit, A extends IAnalyzeUnit, F> {
    /**
     * Returns a categorization of given parse result.
     * 
     * @param langImpl
     *            Language implementation that contains the categorization logic.
     * @param parseResult
     *            Parse result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not overlap and are iterated
     *         over in ascending order.
     */
    Iterable<IRegionCategory<F>> categorize(ILanguageImpl langImpl, P result);

    /**
     * Returns a categorization of given analysis result.
     * 
     * @param langImpl
     *            Language implementation that contains the categorization logic.
     * @param analysisResult
     *            Analysis result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not overlap and are iterated
     *         over in ascending order.
     */
    Iterable<IRegionCategory<F>> categorize(ILanguageImpl langImpl, A result);
}
