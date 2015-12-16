package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for semantic analysis of parsed files, and retrieving origin information of analyzed fragments.
 *
 * @param <P>
 *            Type of the parse result.
 * @param <A>
 *            Type of the analysis result.
 */
public interface IAnalyzer<P, A> {
    /**
     * Performs semantic analysis on given parsed resources, using analysis rules from given language.
     * 
     * @param inputs
     *            Parsed input files.
     * @param context
     *            Context in which the analysis is performed.
     * @return Result of the analysis.
     * @throws AnalysisException
     *             when analysis fails.
     */
    public abstract AnalysisResult<P, A> analyze(Iterable<ParseResult<P>> inputs, IContext context)
        throws AnalysisException;
}
