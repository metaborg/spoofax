package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;

public interface IMultiFileScopeGraphContext extends ISpoofaxScopeGraphContext<IMultiFileScopeGraphUnit> {

    void setInitialResult(InitialResult result);

    Optional<InitialResult> initialResult();

    void setSolution(Solution solution);

    void setCustomSolution(CustomSolution solution);

    void setFinalResult(FinalResult result);

    Optional<FinalResult> finalResult();

    void clear();

}