package org.metaborg.core.analysis;

import org.metaborg.core.language.IFacet;

public class AnalyzerFacet<P, A> implements IFacet {
    public final IAnalyzer<P, A> analyzer;


    public AnalyzerFacet(IAnalyzer<P, A> analyzer) {
        this.analyzer = analyzer;
    }
}
