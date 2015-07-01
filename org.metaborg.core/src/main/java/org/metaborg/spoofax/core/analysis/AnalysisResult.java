package org.metaborg.spoofax.core.analysis;

import java.io.Serializable;

import org.metaborg.spoofax.core.context.IContext;

public class AnalysisResult<ParseT, AnalysisT> implements Serializable {
    private static final long serialVersionUID = -5857696366569348427L;

    public final IContext context;
    public final Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults;
    public final IAnalyzerData analyzerData;


    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<ParseT, AnalysisT>> fileResults,
        IAnalyzerData analyzerData) {
        this.context = context;
        this.fileResults = fileResults;
        this.analyzerData = analyzerData;
    }
}
