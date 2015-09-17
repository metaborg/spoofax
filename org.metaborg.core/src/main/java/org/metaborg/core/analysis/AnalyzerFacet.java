package org.metaborg.core.analysis;

import org.metaborg.core.language.IFacet;

public class AnalyzerFacet implements IFacet {
    public final String analyzerName;


    public AnalyzerFacet(String analyzerName) {
        this.analyzerName = analyzerName;
    }
}
