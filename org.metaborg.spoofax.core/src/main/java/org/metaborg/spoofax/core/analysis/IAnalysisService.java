package org.metaborg.spoofax.core.analysis;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.parser.ParseResult;

/**
 * Interface for semantic analysis of parsed files.
 *
 * @param <ParseT>
 *            Type of the parse result.
 * @param <AnalysisT>
 *            Type of the analysis result.
 */
public interface IAnalysisService<ParseT, AnalysisT> {
    /**
     * Performs semantic analysis on given parsed files.
     * 
     * @param inputs
     *            Parsed input files.
     * @param language
     *            Language to perform analysis with.
     * @return Result of the analysis.
     * @throws SpoofaxException
     *             when analysis fatally fails.
     */
    public abstract AnalysisResult<ParseT, AnalysisT> analyze(Iterable<ParseResult<ParseT>> inputs,
        ILanguage language) throws SpoofaxException;
}