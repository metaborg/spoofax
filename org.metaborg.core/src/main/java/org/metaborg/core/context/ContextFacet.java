package org.metaborg.core.context;

import org.metaborg.core.language.IFacet;

public class ContextFacet implements IFacet {
    private final IContextStrategy strategy;


    public ContextFacet(IContextStrategy strategy) {
        this.strategy = strategy;
    }


    public IContextStrategy strategy() {
        return strategy;
    }
}
