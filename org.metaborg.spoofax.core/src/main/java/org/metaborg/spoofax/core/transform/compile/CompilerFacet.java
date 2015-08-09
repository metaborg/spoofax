package org.metaborg.spoofax.core.transform.compile;

import org.metaborg.core.language.IFacet;

public class CompilerFacet implements IFacet {
    public final String strategyName;


    public CompilerFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
