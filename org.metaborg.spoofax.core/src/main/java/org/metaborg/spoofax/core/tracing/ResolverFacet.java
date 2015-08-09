package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class ResolverFacet implements IFacet {
    public final String strategyName;


    public ResolverFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
