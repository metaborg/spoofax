package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.meta.nabl2.solver.ISolution;
import org.metaborg.meta.nabl2.spoofax.FinalResult;
import org.metaborg.meta.nabl2.spoofax.InitialResult;
import org.metaborg.meta.nabl2.spoofax.UnitResult;
import org.metaborg.spoofax.core.context.scopegraph.MultiFileScopeGraphContext.State;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class MultiFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements IMultiFileScopeGraphContext {

    public MultiFileScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        super(injector, identifier);
    }

    @Override
    protected State initState() {
        return new State();
    }

    @Override
    public IMultiFileScopeGraphUnit unit(String resource) {
        IMultiFileScopeGraphUnit unit;
        if((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource)));
        }
        return unit;
    }

    @Override
    public void removeUnit(String resource) {
        state.units.remove(resource);
    }

    @Override
    public Collection<IMultiFileScopeGraphUnit> units() {
        return state.units.values();
    }

    @Override
    public void setInitialResult(InitialResult result) {
        state.initialResult = Optional.of(result);
    }

    @Override
    public Optional<InitialResult> initialResult() {
        return state.initialResult;
    }

    @Override
    public void setSolution(ISolution solution) {
        state.solution = Optional.of(solution);
    }

    @Override
    public void setFinalResult(FinalResult result) {
        state.finalResult = Optional.of(result);
    }

    @Override
    public Optional<FinalResult> finalResult() {
        return state.finalResult;
    }

    @Override
    public void clear() {
        state.clear();
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8133657561476824164L;

        final Map<String, IMultiFileScopeGraphUnit> units = Maps.newHashMap();

        Optional<InitialResult> initialResult;
        Optional<ISolution> solution;
        Optional<FinalResult> finalResult;

        public State() {
            clear();
        }

        public void clear() {
            this.initialResult = Optional.empty();
            this.solution = Optional.empty();
            this.finalResult = Optional.empty();
        }

        class Unit implements IMultiFileScopeGraphUnit {

            private static final long serialVersionUID = 1176844388074495439L;

            private final String resource;

            private Optional<UnitResult> unitResult;

            private Unit(String resource) {
                this.resource = resource;
                clear();
            }

            @Override
            public String resource() {
                return resource;
            }

            @Override
            public void setUnitResult(UnitResult result) {
                unitResult = Optional.of(result);
            }

            @Override
            public Optional<UnitResult> unitResult() {
                return unitResult;
            }

            @Override
            public Optional<ISolution> solution() {
                return solution;
            }

            @Override
            public void clear() {
                this.unitResult = Optional.empty();
            }

        }

    }

}
