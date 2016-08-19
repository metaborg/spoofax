package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;

/**
 * Result of the analysis of a single parse unit.
 * 
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 */
public interface IAnalyzeResult<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    /**
     * @return Resulting analyze unit of the analysis.
     */
    A result();

    /**
     * @return Updates to existing analyze units.
     */
    Collection<AU> updates();

    /**
     * @return The context that was used during analysis.
     */
    IContext context();
}
