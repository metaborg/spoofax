package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.spoofax.core.context.scopegraph.MultiFileScopeGraphContext.State;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class MultiFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements IMultiFileScopeGraphContext {

    public MultiFileScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        super(injector, identifier);
    }

    @Override protected State initState() {
        return new State();
    }

    @Override public IMultiFileScopeGraphUnit unit(String resource) {
        IMultiFileScopeGraphUnit unit;
        if((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource)));
        }
        return unit;
    }

    @Override public void removeUnit(String resource) {
        state.units.remove(resource);
    }

    @Override public Collection<IMultiFileScopeGraphUnit> units() {
        return state.units.values();
    }

    @Override public void setInitialResult(InitialResult result) {
        state.initialResult = result;
    }

    @Override public Optional<InitialResult> initialResult() {
        return Optional.ofNullable(state.initialResult);
    }

    @Override public void setSolution(Solution solution) {
        state.solution = solution;
    }

    @Override public void setCustomSolution(CustomSolution solution) {
        state.customSolution = solution;
    }

    @Override public void setFinalResult(FinalResult result) {
        state.finalResult = result;
    }

    @Override public Optional<FinalResult> finalResult() {
        return Optional.ofNullable(state.finalResult);
    }

    @Override public void clear() {
        state.clear();
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8133657561476824164L;

        final Map<String,IMultiFileScopeGraphUnit> units = Maps.newHashMap();

        InitialResult initialResult;
        Solution solution;
        CustomSolution customSolution;
        FinalResult finalResult;

        public State() {
            clear();
        }

        public void clear() {
            this.initialResult = null;
            this.solution = null;
            this.customSolution = null;
            this.finalResult = null;
        }

        class Unit implements IMultiFileScopeGraphUnit {

            private static final long serialVersionUID = 1176844388074495439L;

            private final String resource;

            private UnitResult unitResult;

            private Unit(String resource) {
                this.resource = resource;
                clear();
            }

            @Override public String resource() {
                return resource;
            }

            @Override public void setUnitResult(UnitResult result) {
                unitResult = result;
            }

            @Override public Optional<UnitResult> unitResult() {
                return Optional.ofNullable(unitResult);
            }

            @Override public Optional<Solution> solution() {
                return Optional.ofNullable(solution);
            }

            @Override public Optional<CustomSolution> customSolution() {
                return Optional.ofNullable(customSolution);
            }

            @Override public void clear() {
                this.unitResult = null;
            }

        }

    }

}
