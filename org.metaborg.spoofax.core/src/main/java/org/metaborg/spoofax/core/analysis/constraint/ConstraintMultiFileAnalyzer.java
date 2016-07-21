package org.metaborg.spoofax.core.analysis.constraint;

import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;

import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-multifile";

    @Inject public ConstraintMultiFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService  termFactoryService
    ) {
        super(analysisCommon, unitService, runtimeService, strategoCommon, termFactoryService, true);
    }

}
