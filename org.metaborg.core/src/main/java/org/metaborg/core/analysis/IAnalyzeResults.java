package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;

public interface IAnalyzeResults<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    Collection<A> results();

    Collection<AU> updates();

    /**
     * @return The context that was used during analysis.
     */
    IContext context();
}
