package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.AnalyzeResults;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef class for {@link AnalyzeResults} with Spoofax interfaces.
 */
public class SpoofaxAnalyzeResults extends AnalyzeResults<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalyzeResults {
    private final @Nullable Object debugData;


    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context, @Nullable Object debugData) {
        super(results, updates, context);
        this.debugData = debugData;
    }

    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, Collection<ISpoofaxAnalyzeUnitUpdate> updates,
        IContext context) {
        this(results, updates, context, null);
    }

    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, IContext context,
        @Nullable Object debugData) {
        super(results, context);
        this.debugData = debugData;
    }

    public SpoofaxAnalyzeResults(Collection<ISpoofaxAnalyzeUnit> results, IContext context) {
        this(results, context, null);
    }

    public SpoofaxAnalyzeResults(IContext context, @Nullable Object debugData) {
        super(context);
        this.debugData = debugData;
    }

    public SpoofaxAnalyzeResults(IContext context) {
        this(context, null);
    }


    @Override public Object debugData() {
        return debugData;
    }
}
