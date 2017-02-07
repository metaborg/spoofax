package org.metaborg.spoofax.core.analysis;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.IAnalyzeResult;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;

/**
 * Typedef interface for {@link IAnalyzeResult} with Spoofax interfaces.
 */
public interface ISpoofaxAnalyzeResult extends IAnalyzeResult<ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {
    /**
     * @return Debug data produced by analysis, or null if there is none.
     */
    @Nullable Object debugData();
}
