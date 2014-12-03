package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import org.metaborg.spoofax.core.language.ILanguage;

public class AnalysisResult {
    public final ILanguage language;
    public final Collection<AnalysisFileResult> fileResults;
    public final Collection<String> affectedPartitions;
    public final AnalysisDebugResult debugResult;
    public final AnalysisTimeResult timeResult;

    public AnalysisResult(ILanguage language, Collection<AnalysisFileResult> fileResults,
        Collection<String> affectedPartitions, AnalysisDebugResult debugResult, AnalysisTimeResult timeResult) {
        this.language = language;
        this.fileResults = fileResults;
        this.affectedPartitions = affectedPartitions;
        this.debugResult = debugResult;
        this.timeResult = timeResult;
    }
}
