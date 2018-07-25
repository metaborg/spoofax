package org.metaborg.spoofax.core.tracing;
import org.metaborg.core.language.IFacet;

public class StrategoResolverFacet implements IFacet {
    public final String strategyName;


    public StrategoResolverFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
