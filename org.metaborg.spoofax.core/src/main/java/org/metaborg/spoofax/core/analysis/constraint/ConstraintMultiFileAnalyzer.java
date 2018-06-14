package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Set;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.terms.util.NotImplementedException;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {

    public static final String name = "constraint-multifile";

    private static final ILogger logger = LoggerUtils.logger(ConstraintMultiFileAnalyzer.class);

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
            final IResourceService resourceService, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService, final ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService,
                unitService);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(final Set<ISpoofaxParseUnit> changed,
            final Set<ISpoofaxParseUnit> removed, final IConstraintContext context, final HybridInterpreter runtime,
            final String strategy, final IProgress progress, ICancel cancel) throws AnalysisException {
        throw new NotImplementedException();
    }


}
