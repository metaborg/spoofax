package org.metaborg.spoofax.core.analysis.taskengine;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisFacet;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class AnalysisFacetFromESV {
    public static @Nullable SpoofaxAnalysisFacet create(IStrategoAppl esv) {
        final String strategyName = analysisStrategy(esv);
        if(strategyName == null) {
            return null;
        }
        return new SpoofaxAnalysisFacet(strategyName);
    }

    private static @Nullable String analysisStrategy(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final String observerFunction = ESVReader.termContents(termAt(strategy, 0));
        return observerFunction;
    }
}
