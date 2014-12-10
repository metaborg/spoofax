package org.metaborg.spoofax.core.analysis;

import org.metaborg.spoofax.core.language.ILanguage;

public class AnalysisResult<ParseT, AnalysisT> {
    public final ILanguage language;
    public final Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults;
    public final Iterable<String> affectedPartitions;
    public final AnalysisDebugResult debugResult;
    public final AnalysisTimeResult timeResult;

    public AnalysisResult(ILanguage language, Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults,
        Iterable<String> affectedPartitions, AnalysisDebugResult debugResult, AnalysisTimeResult timeResult) {
        this.language = language;
        this.fileResults = fileResults;
        this.affectedPartitions = affectedPartitions;
        this.debugResult = debugResult;
        this.timeResult = timeResult;
    }
}
