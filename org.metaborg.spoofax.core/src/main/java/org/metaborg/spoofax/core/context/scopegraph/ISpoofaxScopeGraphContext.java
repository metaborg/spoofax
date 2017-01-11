package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.IContextInternal;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphContext;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphUnit;

public interface ISpoofaxScopeGraphContext<U extends IScopeGraphUnit> extends IContextInternal, IScopeGraphContext<U> {

    /** Remove unit from the context */
    void removeUnit(String resource);

}