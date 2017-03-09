package org.metaborg.core.analysis;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.processing.NullCancel;
import org.metaborg.core.processing.NullProgress;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

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
    default IAnalyzeResult<A, AU> analyze(P input, IContext context) throws AnalysisException {
        try {
            return analyze(input, context, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
    }

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
    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context, IProgress progress, ICancel cancel)
        throws AnalysisException, InterruptedException;

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
     */
    default IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException {
        try {
            return analyzeAll(inputs, context, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
    }
}
