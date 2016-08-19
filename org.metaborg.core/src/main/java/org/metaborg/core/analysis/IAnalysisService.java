package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for context-sensitive analysis of parse units.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 */
public interface IAnalysisService<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    /**
     * Checks if analysis is available for given language implementation.
     * 
     * @param langImpl
     *            Language implementation to check.
     * @return True if analysis is a available, false if not.
     */
    boolean available(ILanguageImpl langImpl);

    /**
     * Analyzes given parse input, in given context, into an analysis result which contains an analyze unit and
     * optionally updates to analyze units.
     * 
     * @param input
     *            Parse unit to analyze.
     * @param context
     *            Context to perform analysis in.
     * @return Analysis result which contains an analyze unit and optionally updates to analyze units.
     * @throws AnalysisException
     *             When analysis fails unexpectedly.
     */
    IAnalyzeResult<A, AU> analyze(P input, IContext context) throws AnalysisException;

    /**
     * Analyzes given parse input, in given context, into an analysis result which contains an analyze unit and
     * optionally updates to analyze units.
     * 
     * @param input
     *            Parse unit to analyze.
     * @param context
     *            Context to perform analysis in.
     * @return Analysis result which contains an analyze unit and optionally updates to analyze units.
     * @throws AnalysisException
     *             When analysis fails unexpectedly.
     */
    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException;
}
