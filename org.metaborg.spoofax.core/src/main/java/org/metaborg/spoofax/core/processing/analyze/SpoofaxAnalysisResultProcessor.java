package org.metaborg.spoofax.core.processing.analyze;

import org.metaborg.core.processing.analyze.AnalysisResultProcessor;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultRequester;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;


/**
 * Typedef class for {@link AnalysisResultProcessor} with Spoofax interfaces.
 */
public class SpoofaxAnalysisResultProcessor extends
    AnalysisResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalysisResultProcessor {
    @jakarta.inject.Inject public SpoofaxAnalysisResultProcessor(ISpoofaxAnalysisService analysisService,
        ISpoofaxParseResultRequester parseResultRequester) {
        super(analysisService, parseResultRequester);
    }
}
