package org.metaborg.spoofax.core.transform;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.transform.TransformService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;


/**
 * Typedef class for {@link TransformService} with {@link IStrategoTerm}.
 */
public class SpoofaxTransformService extends
    TransformService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>
    implements ISpoofaxTransformService {
    @jakarta.inject.Inject @javax.inject.Inject public SpoofaxTransformService(IActionService actionService, ISpoofaxAnalysisService analysisService,
            IStrategoTransformer transformer) {
        super(actionService, analysisService, transformer);
    }
}
