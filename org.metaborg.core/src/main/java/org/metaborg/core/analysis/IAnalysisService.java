package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for semantic analysis of parsed files, and retrieving origin information of analyzed fragments.
 *
 * @param <ParseT>
 *            Type of the parse result.
 * @param <AnalysisT>
 *            Type of the analysis result.
 */
public interface IAnalysisService<ParseT, AnalysisT> {
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
    public abstract AnalysisResult<ParseT, AnalysisT> analyze(Iterable<ParseResult<ParseT>> inputs, IContext context)
        throws AnalysisException;
}