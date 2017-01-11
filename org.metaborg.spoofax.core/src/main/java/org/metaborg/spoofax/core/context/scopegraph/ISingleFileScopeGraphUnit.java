package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphUnit;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;

public interface ISingleFileScopeGraphUnit extends IScopeGraphUnit {

    Optional<InitialResult> initialResult();

    void setInitialResult(InitialResult result);

    Optional<UnitResult> unitResult();

    void setUnitResult(UnitResult result);

    void setSolution(Solution solution);

    void setCustomSolution(CustomSolution solution);

    Optional<FinalResult> finalResult();

    void setFinalResult(FinalResult result);

    void clear();

}