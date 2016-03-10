package org.metaborg.spoofax.core.processing.analyze;

import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IAnalysisResultProcessor} with Spoofax interfaces.
 */
public interface ISpoofaxAnalysisResultProcessor
    extends IAnalysisResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>,
    ISpoofaxAnalysisResultRequester, ISpoofaxAnalysisResultUpdater {

}
