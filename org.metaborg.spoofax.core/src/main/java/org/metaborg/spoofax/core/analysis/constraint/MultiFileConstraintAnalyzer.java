package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;

import com.google.inject.Inject;

public class MultiFileConstraintAnalyzer extends AbstractConstraintAnalyzer {

    public static final String name = "constraint-multifile";

    @Inject public MultiFileConstraintAnalyzer(AnalysisCommon analysisCommon, IResourceService resourceService,
            IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon,
            ITermFactoryService termFactoryService, ISpoofaxTracingService tracingService,
            ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService,
            unitService);
    }

    @Override protected boolean multifile() {
        return true;
    }
}