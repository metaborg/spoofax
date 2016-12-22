package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.IContextInternal;
import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.context.IScopeGraphUnit;

public interface ISpoofaxScopeGraphContext<U extends IScopeGraphUnit> extends IContextInternal, IScopeGraphContext<U> {

    /** Remove unit from the context */
    void removeUnit(String resource);

}