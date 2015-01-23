package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ParseResult;

/**
 * Interface for categorization of parse and analysis results.
 * 
 * @param <ParseT>
 *            Type of the parsed fragments.
 * @param <AnalysisT>
 *            Type of the analyzed fragments.
 */
public interface ICategorizerService<ParseT, AnalysisT> {
    /**
     * Returns a categorization of given parse result.
     * 
     * @param language
     *            Language that contains the categorization logic.
     * @param parseResult
     *            Parse result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not
     *         overlap and are iterated over in ascending order.
     */
    public abstract Iterable<IRegionCategory<ParseT>> categorize(ILanguage language,
        ParseResult<ParseT> parseResult);

    /**
     * Returns a categorization of given analysis result.
     * 
     * @param language
     *            Language that contains the categorization logic.
     * @param analysisResult
     *            Analysis result to categorize.
     * @return Iterable over categories assigned to regions of the source text. Regions do not
     *         overlap and are iterated over in ascending order.
     */
    public abstract Iterable<IRegionCategory<AnalysisT>> categorize(ILanguage language,
        AnalysisFileResult<ParseT, AnalysisT> analysisResult);
}
