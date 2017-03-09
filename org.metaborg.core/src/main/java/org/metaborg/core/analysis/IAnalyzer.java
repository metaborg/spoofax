package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

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
     * @param progress
     *            Progress reporter.
     * @param cancel
     *            Cancellation token.
     * @return Analysis result which contains an analyze unit and optionally updates to analyze units.
     * @throws AnalysisException
     *             When analysis fails unexpectedly.
     * @throws InterruptedException
     *             When analysis is cancelled.
     */
    IAnalyzeResult<A, AU> analyze(P input, IContext context, IProgress progress, ICancel cancel)
        throws AnalysisException, InterruptedException;

    /**
     * Analyzes given parse inputs, in given context, into an analysis result which contains analyze units and
     * optionally updates to analyze units.
     * 
     * @param inputs
     *            Parse units to analyze.
     * @param context
     *            Context to perform analysis in.
     * @param progress
     *            Progress reporter.
     * @param cancel
     *            Cancellation token.
     * @return Analysis result which contains analyze units and optionally updates to analyze units.
     * @throws AnalysisException
     *             When analysis fails unexpectedly.
     * @throws InterruptedException
     *             When analysis is cancelled.
     */
    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context, IProgress progress, ICancel cancel)
        throws AnalysisException, InterruptedException;
}
