package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.IContext;
import org.metaborg.nabl2.context.IScopeGraphContext;

public interface ISpoofaxScopeGraphContext extends IContext, IScopeGraphContext<ISpoofaxScopeGraphUnit> {

    ISpoofaxScopeGraphUnit getOrCreateUnit(String source);

}
