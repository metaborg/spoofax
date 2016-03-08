package org.metaborg.core.analysis;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.syntax.IParseUnit;

public class AnalyzerFacet<P extends IParseUnit, A extends IAnalyzeUnit> implements IFacet {
    public final IAnalyzer<P, A> analyzer;


    public AnalyzerFacet(IAnalyzer<P, A> analyzer) {
        this.analyzer = analyzer;
    }
}
