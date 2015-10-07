package org.metaborg.spoofax.core.completion;

import org.metaborg.core.language.IFacet;

public class SemanticCompletionFacet implements IFacet {
    public final String strategyName;


    public SemanticCompletionFacet(String strategyName) {
        this.strategyName = strategyName;
    }
}
