package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.ITemporaryContext;

public interface IScopeGraphContext extends IContext, ITemporaryContext {
    @Nullable IScopeGraphInitial initial();
    Collection<IScopeGraphUnit> units();
}
