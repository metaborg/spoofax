package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import org.metaborg.core.analysis.AnalyzeResults;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef class for {@link AnalyzeResults} with Spoofax interfaces.
 */
public class SpoofaxAnalyzeResults extends AnalyzeResults<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalyzeResults {
    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context) {
        super(results, updates, context);
    }

    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, IContext context) {
        super(results, context);
    }

    public SpoofaxAnalyzeResults(IContext context) {
        super(context);
    }
}
