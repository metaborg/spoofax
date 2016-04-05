package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import org.metaborg.core.analysis.AnalyzeResult;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef class for {@link AnalyzeResult} with Spoofax interfaces.
 */
public class SpoofaxAnalyzeResult extends AnalyzeResult<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalyzeResult {
    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context) {
        super(result, updates, context);
    }

    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, IContext context) {
        super(result, context);
    }
}
