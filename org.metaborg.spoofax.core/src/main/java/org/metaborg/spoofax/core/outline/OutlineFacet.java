package org.metaborg.spoofax.core.outline;

import org.metaborg.core.language.IFacet;

public class OutlineFacet implements IFacet {
    public final String strategyName;
    public final int expandTo;


    public OutlineFacet(String strategyName, int expandTo) {
        this.strategyName = strategyName;
        this.expandTo = expandTo;
    }
}
