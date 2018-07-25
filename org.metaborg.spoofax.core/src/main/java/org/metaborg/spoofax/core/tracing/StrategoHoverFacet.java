package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class StrategoHoverFacet implements IFacet {
    public final String strategyName;


    public StrategoHoverFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
