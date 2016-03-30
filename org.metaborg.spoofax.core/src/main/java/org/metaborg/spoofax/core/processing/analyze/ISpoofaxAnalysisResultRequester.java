package org.metaborg.spoofax.core.processing.analyze;

import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;

/**
 * Typedef interface for {@link IAnalysisResultRequester} with Spoofax interfaces.
 */
public interface ISpoofaxAnalysisResultRequester
    extends IAnalysisResultRequester<ISpoofaxInputUnit, ISpoofaxAnalyzeUnit> {

}
