package org.metaborg.spoofax.core.analysis;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.IAnalyzeResults;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef interface for {@link IAnalyzeResults} with Spoofax interfaces.
 */
public interface ISpoofaxAnalyzeResults extends IAnalyzeResults<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {
    /**
     * @return Debug data produced by analysis, or null if there is none.
     */
    @Nullable Object debugData();
}
