package org.metaborg.spoofax.core.transform.stratego.compile;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;

public class CompilerFacet implements ILanguageFacet {
    public final @Nullable String strategyName;


    public CompilerFacet() {
        this(null);
    }

    public CompilerFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
