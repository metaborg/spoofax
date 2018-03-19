package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.CustomSolution;
import mb.nabl2.spoofax.analysis.FinalResult;
import mb.nabl2.spoofax.analysis.InitialResult;

public interface IMultiFileScopeGraphContext extends ISpoofaxScopeGraphContext<IMultiFileScopeGraphUnit> {

    void setInitialResult(InitialResult result);

    Optional<InitialResult> initialResult();

    void setInitialSolution(ISolution solution);

    Optional<ISolution> initialSolution();

    void setSolution(ISolution solution);

    void setCustomSolution(CustomSolution solution);

    void setFinalResult(FinalResult result);

    Optional<FinalResult> finalResult();

    void clear();

}