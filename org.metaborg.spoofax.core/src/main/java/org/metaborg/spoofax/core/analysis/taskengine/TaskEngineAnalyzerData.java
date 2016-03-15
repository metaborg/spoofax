package org.metaborg.spoofax.core.analysis.taskengine;

public class TaskEngineAnalyzerData {
    public final Iterable<String> affectedPartitions;
    public final AnalysisDebugResult debugResult;
    public final AnalysisTimeResult timeResult;


    public TaskEngineAnalyzerData(Iterable<String> affectedPartitions, AnalysisDebugResult debugResult,
        AnalysisTimeResult timeResult) {
        this.affectedPartitions = affectedPartitions;
        this.debugResult = debugResult;
        this.timeResult = timeResult;
    }
}
