package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.CustomSolution;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;
import mb.nabl2.spoofax.analysis.UnitResult;

public interface IMultiFileScopeGraphUnit extends IScopeGraphUnit {

    void setUnitResult(UnitResult result);

    Optional<UnitResult> unitResult();

    void setPartialSolution(ISolution solution);

    Optional<ISolution> partialSolution();

    void setSolution(ISolution solution);

    void setCustomSolution(CustomSolution solution);

    void clear();

}