package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;

/**
 * Result of the analysis of a multiple parse units.
 * 
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 */
public interface IAnalyzeResults<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    /**
     * @return Resulting analyze units of the analysis.
     */
    Collection<A> results();

    /**
     * @return Updates to existing analyze units.
     */
    Collection<AU> updates();

    /**
     * @return The context that was used during analysis.
     */
    IContext context();
}
