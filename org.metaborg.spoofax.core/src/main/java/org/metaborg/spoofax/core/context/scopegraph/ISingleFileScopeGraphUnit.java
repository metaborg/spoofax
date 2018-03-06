package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.CustomSolution;
import mb.nabl2.spoofax.analysis.FinalResult;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;
import mb.nabl2.spoofax.analysis.InitialResult;
import mb.nabl2.spoofax.analysis.UnitResult;

public interface ISingleFileScopeGraphUnit extends IScopeGraphUnit {

    Optional<InitialResult> initialResult();

    void setInitialResult(InitialResult result);

    Optional<UnitResult> unitResult();

    void setUnitResult(UnitResult result);

    void setSolution(ISolution solution);

    void setCustomSolution(CustomSolution solution);

    Optional<FinalResult> finalResult();

    void setFinalResult(FinalResult result);

    void clear();

}