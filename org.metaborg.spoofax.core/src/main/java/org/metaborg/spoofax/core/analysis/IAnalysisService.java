package org.metaborg.spoofax.core.analysis;

import java.io.File;
import java.util.Collection;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.parser.ParseResult;

public interface IAnalysisService<ParseT, AnalysisT> {
    /**
     * Run the analysis on the given files. The analysis is started on all files on a per-language basis.
     * 
     * @see #analyze(File)
     * @param inputs
     * @throws SpoofaxException
     */
    public abstract Collection<AnalysisResult<ParseT, AnalysisT>> analyze(
        Collection<ParseResult<ParseT>> inputs) throws SpoofaxException;
}