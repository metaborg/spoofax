package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultRequester;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.ITermFactory;


public class MultiFileConstraintAnalyzer extends AbstractConstraintAnalyzer {

    public static final String name = "constraint-multifile";

    @jakarta.inject.Inject @javax.inject.Inject public MultiFileConstraintAnalyzer(AnalysisCommon analysisCommon,
            final ISpoofaxAnalysisResultRequester analysisResultRequester, IResourceService resourceService,
            IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon, ITermFactory termFactory,
            ISpoofaxTracingService tracingService, ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactory, tracingService,
                unitService);
    }

    @Override protected boolean multifile() {
        return true;
    }

}