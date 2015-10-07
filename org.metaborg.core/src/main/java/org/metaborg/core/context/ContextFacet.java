package org.metaborg.core.context;

import org.metaborg.core.language.IFacet;

public class ContextFacet implements IFacet {
    public final IContextFactory factory;
    public final IContextStrategy strategy;


    public ContextFacet(IContextFactory factory, IContextStrategy strategy) {
        this.factory = factory;
        this.strategy = strategy;
    }
}
