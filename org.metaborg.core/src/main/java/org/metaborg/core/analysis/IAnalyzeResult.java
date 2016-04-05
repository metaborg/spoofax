package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;

public interface IAnalyzeResult<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    A result();

    Collection<AU> updates();

    /**
     * @return The context that was used during analysis.
     */
    IContext context();
}
