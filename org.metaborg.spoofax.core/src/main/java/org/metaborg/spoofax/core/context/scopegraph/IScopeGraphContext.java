package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.ITemporaryContext;

public interface IScopeGraphContext extends IContext, ITemporaryContext {

    /** Add a unit to this context */
    void addUnit(IScopeGraphUnit unit);

    /** Get unit for the given resource */
    @Nullable IScopeGraphUnit unit(String source);

    /** Get all units in this context */
    Iterable<IScopeGraphUnit> units();

    /** Remove a unit from this context */
    void removeUnit(String source);

}