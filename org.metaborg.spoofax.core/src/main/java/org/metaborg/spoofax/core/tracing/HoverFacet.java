package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class HoverFacet implements IFacet {
    public final String strategyName;


    public HoverFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
