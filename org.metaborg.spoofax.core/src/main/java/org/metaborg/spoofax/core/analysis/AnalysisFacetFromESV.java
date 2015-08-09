package org.metaborg.spoofax.core.analysis;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AnalysisFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(AnalysisFacetFromESV.class);

    public static @Nullable AnalysisFacet create(IStrategoAppl esv) {
        final String strategyName = analysisStrategy(esv);
        if(strategyName == null) {
            return null;
        }
        final StrategoAnalysisMode mode = analysisMode(esv);
        if(mode == null) {
            logger.error("Unable to retrieve analysis mode for analysis strategy {}, cannot create analysis facet",
                strategyName);
            return null;
        }
        return new AnalysisFacet(strategyName, mode);
    }

    private static @Nullable String analysisStrategy(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final String observerFunction = ESVReader.termContents(termAt(strategy, 0));
        return observerFunction;
    }

    private static @Nullable StrategoAnalysisMode analysisMode(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final IStrategoTerm annotations = strategy.getSubterm(1);
        boolean multifile = false;
        for(IStrategoTerm annotation : annotations) {
            multifile |= Tools.hasConstructor((IStrategoAppl) annotation, "MultiFile", 0);
        }
        if(multifile) {
            return StrategoAnalysisMode.MultiAST;
        }
        return StrategoAnalysisMode.SingleAST;
    }
}
