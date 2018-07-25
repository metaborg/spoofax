package org.metaborg.spoofax.core.outline;

public class StrategoOutlineFacet implements IOutlineFacet {
    public final String strategyName;
    public final int expandTo;


    public StrategoOutlineFacet(String strategyName, int expandTo) {
        this.strategyName = strategyName;
        this.expandTo = expandTo;
    }


    @Override
    public int getExpansionLevel() {
        return expandTo;
    }
}
