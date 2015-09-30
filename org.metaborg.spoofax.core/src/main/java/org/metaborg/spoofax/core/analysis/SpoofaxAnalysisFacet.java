package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.language.IFacet;

public class SpoofaxAnalysisFacet implements IFacet {
    public final String strategyName;


    public SpoofaxAnalysisFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
