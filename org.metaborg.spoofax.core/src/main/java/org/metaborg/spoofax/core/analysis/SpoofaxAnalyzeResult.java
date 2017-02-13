package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.AnalyzeResult;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef class for {@link AnalyzeResult} with Spoofax interfaces.
 */
public class SpoofaxAnalyzeResult extends AnalyzeResult<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalyzeResult {
    private final @Nullable Object debugData;


    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context, @Nullable Object debugData) {
        super(result, updates, context);
        this.debugData = debugData;
    }

    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context) {
        this(result, updates, context, null);
    }

    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, IContext context, @Nullable Object debugData) {
        super(result, context);
        this.debugData = debugData;
    }

    public SpoofaxAnalyzeResult(ISpoofaxAnalyzeUnit result, IContext context) {
        this(result, context, null);
    }


    @Override public Object debugData() {
        return debugData;
    }
}
