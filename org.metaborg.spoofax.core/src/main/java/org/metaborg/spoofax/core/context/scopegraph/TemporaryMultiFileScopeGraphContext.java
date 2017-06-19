package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import org.metaborg.meta.nabl2.solver.ISolution;
import org.metaborg.meta.nabl2.solver.solvers.IncrementalMultiFileSolver.IncrementalSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;

public class TemporaryMultiFileScopeGraphContext extends AbstractTemporaryScopeGraphContext<IMultiFileScopeGraphUnit>
        implements IMultiFileScopeGraphContext {

    private final IMultiFileScopeGraphContext context;

    public TemporaryMultiFileScopeGraphContext(IMultiFileScopeGraphContext context) {
        super(context);
        this.context = context;
    }

    @Override public void clear() {
        context.clear();
    }

    @Override public void setInitialResult(InitialResult result) {
        context.setInitialResult(result);
    }

    @Override public Optional<InitialResult> initialResult() {
        return context.initialResult();
    }

    public void setInitialSolution(ISolution solution) {
        context.setInitialSolution(solution);
    }

    public Optional<ISolution> initialSolution() {
        return context.initialSolution();
    }

    public void setIncrementalSolution(IncrementalSolution solution) {
        context.setIncrementalSolution(solution);
    }

    public Optional<IncrementalSolution> incrementalSolution() {
        return context.incrementalSolution();
    }

    @Override public void setSolution(ISolution solution) {
        context.setSolution(solution);
    }

    public void setCustomSolution(CustomSolution solution) {
        context.setCustomSolution(solution);
    }

    @Override public void setFinalResult(FinalResult result) {
        context.setFinalResult(result);
    }

    @Override public Optional<FinalResult> finalResult() {
        return context.finalResult();
    }

}