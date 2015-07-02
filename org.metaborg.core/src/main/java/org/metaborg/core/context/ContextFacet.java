package org.metaborg.core.context;

import org.metaborg.core.language.ILanguageFacet;

public class ContextFacet implements ILanguageFacet {
    private static final long serialVersionUID = -992751010335621557L;

    private final IContextStrategy strategy;


    public ContextFacet(IContextStrategy strategy) {
        this.strategy = strategy;
    }


    public IContextStrategy strategy() {
        return strategy;
    }
}
