package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.language.IFacet;

public class AnalysisFacet implements IFacet {
    public final String strategyName;
    public final StrategoAnalysisMode mode;


    public AnalysisFacet(String analysisStrategy, StrategoAnalysisMode analysisMode) {
        this.strategyName = analysisStrategy;
        this.mode = analysisMode;
    }
}
