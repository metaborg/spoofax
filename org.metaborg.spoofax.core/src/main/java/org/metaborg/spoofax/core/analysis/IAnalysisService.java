package org.metaborg.spoofax.core.analysis;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ParseResult;

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
     * @throws SpoofaxException
     *             when analysis fatally fails.
     */
    public abstract AnalysisResult<ParseT, AnalysisT> analyze(Iterable<ParseResult<ParseT>> inputs,
        IContext context) throws SpoofaxException;

    /**
     * Returns the origin fragment for given analyzed fragment.
     * 
     * @param analyzed
     *            Analyzed fragment
     * 
     * @return Origin fragment for analyzed fragment.
     */
    public abstract ParseT origin(AnalysisT analyzed);

    /**
     * Attempts to retrieve the source region for given analyzed fragment.
     * 
     * @param analyzed
     *            Analyzed fragment
     * 
     * @return Source region for analyzed fragment, or null if no source region can be retrieved.
     */
    public abstract @Nullable ISourceRegion region(AnalysisT analyzed);
}