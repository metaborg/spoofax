package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.language.IFacet;

public class AnalysisFacet implements IFacet {
    public final String strategyName;


    public AnalysisFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
