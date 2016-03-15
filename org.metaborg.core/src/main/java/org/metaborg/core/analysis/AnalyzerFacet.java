package org.metaborg.core.analysis;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.syntax.IParseUnit;

public class AnalyzerFacet<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate>
    implements IFacet {
    public final IAnalyzer<P, A, AU> analyzer;


    public AnalyzerFacet(IAnalyzer<P, A, AU> analyzer) {
        this.analyzer = analyzer;
    }
}
