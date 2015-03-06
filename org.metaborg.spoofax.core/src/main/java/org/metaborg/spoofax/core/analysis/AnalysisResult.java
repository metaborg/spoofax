package org.metaborg.spoofax.core.analysis;

import org.metaborg.spoofax.core.context.IContext;

public class AnalysisResult<ParseT, AnalysisT> {
    public final IContext context;
    public final Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults;
    public final Iterable<String> affectedPartitions;
    public final AnalysisDebugResult debugResult;
    public final AnalysisTimeResult timeResult;


    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults,
        Iterable<String> affectedPartitions, AnalysisDebugResult debugResult, AnalysisTimeResult timeResult) {
        this.context = context;
        this.fileResults = fileResults;
        this.affectedPartitions = affectedPartitions;
        this.debugResult = debugResult;
        this.timeResult = timeResult;
    }
}
