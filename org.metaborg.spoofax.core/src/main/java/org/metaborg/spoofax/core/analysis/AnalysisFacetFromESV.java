package org.metaborg.spoofax.core.analysis;

import static org.spoofax.interpreter.core.Tools.termAt;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.analysis.constraint.MultiFileConstraintAnalyzer;
import org.metaborg.spoofax.core.analysis.constraint.SingleFileConstraintAnalyzer;
import org.metaborg.spoofax.core.analysis.legacy.StrategoAnalyzer;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AnalysisFacetFromESV {
    public static boolean hasAnalysis(IStrategoAppl esv) {
        return ESVReader.findTerm(esv, "SemanticObserver") != null;
    }
    
    public static @Nullable AnalysisFacet create(IStrategoAppl esv) {
        final String strategyName = strategyName(esv);
        if(strategyName == null) {
            return null;
        }
        return new AnalysisFacet(strategyName);
    }

    private static @Nullable String strategyName(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final String observerFunction = ESVReader.termContents(termAt(strategy, 0));
        return observerFunction;
    }


    public static @Nullable String type(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final IStrategoTerm annotations = strategy.getSubterm(1);
        boolean multifile  = false;
        boolean constraint = false;
        for(IStrategoTerm annotation : annotations) {
            multifile  |= Tools.hasConstructor((IStrategoAppl) annotation, "MultiFile", 0);
            constraint |= Tools.hasConstructor((IStrategoAppl) annotation, "Constraint", 0);
        }
        if(constraint) {
            return multifile ? MultiFileConstraintAnalyzer.name : SingleFileConstraintAnalyzer.name;
        } else if(multifile) {
            return TaskEngineAnalyzer.name;
        }
        return StrategoAnalyzer.name;
    }
}
