package org.metaborg.core.analysis;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContext;
import org.metaborg.util.iterators.Iterables2;

public class AnalysisResult<P, A> implements Serializable {
    private static final long serialVersionUID = -5857696366569348427L;

    public final IContext context;
    public final Iterable<AnalysisFileResult<P, A>> fileResults;
    public final Iterable<AnalysisMessageResult> messageResults;
    public final @Nullable IAnalyzerData analyzerData;


    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<P, A>> fileResults,
        Iterable<AnalysisMessageResult> messageResults, @Nullable IAnalyzerData analyzerData) {
        this.context = context;
        this.fileResults = fileResults;
        this.messageResults = messageResults;
        this.analyzerData = analyzerData;
    }

    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<P, A>> fileResults,
        Iterable<AnalysisMessageResult> messageResults) {
        this(context, fileResults, messageResults, null);
    }

    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<P, A>> fileResults,
        @Nullable IAnalyzerData analyzerData) {
        this(context, fileResults, Iterables2.<AnalysisMessageResult>empty(), analyzerData);
    }

    public AnalysisResult(IContext context, Iterable<AnalysisFileResult<P, A>> fileResults) {
        this(context, fileResults, Iterables2.<AnalysisMessageResult>empty(), null);
    }

    public AnalysisResult(IContext context) {
        this(context, Iterables2.<AnalysisFileResult<P, A>>empty(), Iterables2.<AnalysisMessageResult>empty(), null);
    }
}
