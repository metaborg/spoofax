package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.analysis.AnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef class for {@link AnalysisService} with Spoofax interfaces.
 */
public class SpoofaxAnalysisService
    extends AnalysisService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>
    implements ISpoofaxAnalysisService {
}
