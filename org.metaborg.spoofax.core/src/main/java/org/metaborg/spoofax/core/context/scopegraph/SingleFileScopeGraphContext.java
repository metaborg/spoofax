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
import org.metaborg.spoofax.core.context.scopegraph.SingleFileScopeGraphContext.State;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class SingleFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements ISingleFileScopeGraphContext {

    public SingleFileScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        super(injector, identifier);
    }

    @Override
    protected State initState() {
        return new State();
    }

    @Override
    public ISingleFileScopeGraphUnit unit(String resource) {
        ISingleFileScopeGraphUnit unit;
        if((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource)));
        }
        return unit;
    }

    @Override
    public Collection<ISingleFileScopeGraphUnit> units() {
        return state.units.values();
    }

    @Override
    public void removeUnit(String resource) {
        state.units.remove(resource);
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8878117069378041686L;

        final Map<String, ISingleFileScopeGraphUnit> units = Maps.newHashMap();

        class Unit implements ISingleFileScopeGraphUnit {

            private static final long serialVersionUID = -2828933828253182233L;

            private final String resource;

            private Optional<InitialResult> initialResult;
            private Optional<UnitResult> unitResult;
            private Optional<ISolution> solution;
            private Optional<FinalResult> finalResult;

            private Unit(String resource) {
                this.resource = resource;
                clear();
            }

            @Override
            public String resource() {
                return resource;
            }

            @Override
            public Optional<InitialResult> initialResult() {
                return initialResult;
            }

            @Override
            public void setInitialResult(InitialResult result) {
                initialResult = Optional.of(result);
            }

            @Override
            public Optional<UnitResult> unitResult() {
                return unitResult;
            }

            @Override
            public void setUnitResult(UnitResult result) {
                unitResult = Optional.of(result);
            }

            @Override
            public Optional<ISolution> solution() {
                return solution;
            }

            @Override
            public void setSolution(ISolution solution) {
                this.solution = Optional.of(solution);
            }

            @Override
            public Optional<FinalResult> finalResult() {
                return finalResult;
            }

            @Override
            public void setFinalResult(FinalResult result) {
                finalResult = Optional.of(result);
            }

            @Override
            public void clear() {
                this.initialResult = Optional.empty();
                this.unitResult = Optional.empty();
                this.solution = Optional.empty();
                this.finalResult = Optional.empty();
            }

        }

    }

}
