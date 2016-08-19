package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for a context-sensitive analyzer implementation.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 */
public interface IAnalyzer<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
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
     * Analyzes given parse inputs, in given context, into an analysis result which contains analyze units and
     * optionally updates to analyze units.
     * 
     * @param inputs
     *            Parse units to analyze.
     * @param context
     *            Context to perform analysis in.
     * @return Analysis result which contains analyze units and optionally updates to analyze units.
     * @throws AnalysisException
     *             When analysis fails unexpectedly.
     */
    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException;
}
