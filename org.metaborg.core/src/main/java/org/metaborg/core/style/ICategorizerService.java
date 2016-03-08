package org.metaborg.core.style;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for categorization of parse and analysis results.
 * 
 * @param <P>
 *            Type of the parsed fragments.
 * @param <A>
 *            Type of the analyzed fragments.
 */
public interface ICategorizerService<P, A> {
    /**
     * Returns a categorization of given parse result.
     * 
     * @param language
     *            Language that contains the categorization logic.
     * @param parseResult
     *            Parse result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not overlap and are iterated
     *         over in ascending order.
     */
    Iterable<IRegionCategory<P>> categorize(ILanguageImpl language, ParseResult<P> parseResult);

    /**
     * Returns a categorization of given analysis result.
     * 
     * @param language
     *            Language that contains the categorization logic.
     * @param analysisResult
     *            Analysis result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not overlap and are iterated
     *         over in ascending order.
     */
    Iterable<IRegionCategory<A>> categorize(ILanguageImpl language,
                                            AnalysisFileResult<P, A> analysisResult);
}
