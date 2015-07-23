package org.metaborg.spoofax.core.transform.compile;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;

public class CompilerFacet implements IFacet {
    public final @Nullable String strategyName;


    public CompilerFacet() {
        this(null);
    }

    public CompilerFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
