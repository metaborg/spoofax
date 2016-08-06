package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

public interface IScopeGraphContext {

    /** Add a unit to this context */
    void addUnit(IScopeGraphUnit unit);

    /** Get unit for the given resource */
    @Nullable IScopeGraphUnit unit(String source);

    /** Get all units in this context */
    Iterable<IScopeGraphUnit> units();

    /** Remove a unit from this context */
    void removeUnit(String source);

}