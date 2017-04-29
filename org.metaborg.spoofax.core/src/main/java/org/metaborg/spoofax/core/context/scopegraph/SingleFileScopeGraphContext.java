package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.Fresh;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.spoofax.core.context.scopegraph.SingleFileScopeGraphContext.State;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

public class SingleFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements ISingleFileScopeGraphContext {

    public SingleFileScopeGraphContext(Injector injector, ContextIdentifier identifier, NaBL2Config config) {
        super(injector, identifier, config);
    }

    @Override protected State initState() {
        return new State();
    }

    @Override public ISingleFileScopeGraphUnit unit(String resource) {
        resource = normalizeResource(resource);
        ISingleFileScopeGraphUnit unit;
        if((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource)));
        }
        return unit;
    }

    @Override public Collection<ISingleFileScopeGraphUnit> units() {
        return state.units.values();
    }

    @Override public void removeUnit(String resource) {
        resource = normalizeResource(resource);
        state.units.remove(resource);
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8878117069378041686L;

        final Map<String, ISingleFileScopeGraphUnit> units = Maps.newHashMap();

        class Unit implements ISingleFileScopeGraphUnit {

            private static final long serialVersionUID = -2828933828253182233L;

            private final String resource;
            private final Fresh fresh;

            private InitialResult initialResult;
            private UnitResult unitResult;
            private Solution solution;
            private CustomSolution customSolution;
            private FinalResult finalResult;

            private Unit(String resource) {
                this.resource = resource;
                this.fresh = new Fresh();
                clear();
            }

            @Override public String resource() {
                return resource;
            }

            @Override public Optional<InitialResult> initialResult() {
                return Optional.ofNullable(initialResult);
            }

            @Override public void setInitialResult(InitialResult result) {
                initialResult = result;
            }

            @Override public Optional<UnitResult> unitResult() {
                return Optional.ofNullable(unitResult);
            }

            @Override public void setUnitResult(UnitResult result) {
                unitResult = result;
            }

            @Override public Set<IConstraint> constraints() {
                final Set<IConstraint> constraints = Sets.newHashSet();
                initialResult().ifPresent(ir -> constraints.addAll(ir.getConstraints()));
                unitResult().ifPresent(ur -> constraints.addAll(ur.getConstraints()));
                return constraints;
            }

            @Override public Optional<Solution> solution() {
                return Optional.ofNullable(solution);
            }

            @Override public void setSolution(Solution solution) {
                this.solution = solution;
            }

            public Optional<CustomSolution> customSolution() {
                return Optional.ofNullable(customSolution);
            }

            public void setCustomSolution(CustomSolution solution) {
                this.customSolution = solution;
            }

            @Override public Optional<FinalResult> finalResult() {
                return Optional.ofNullable(finalResult);
            }

            @Override public void setFinalResult(FinalResult result) {
                finalResult = result;
            }

            @Override public Fresh fresh() {
                return fresh;
            }

            @Override public boolean isPrimary() {
                return true;
            }

            @Override public void clear() {
                this.initialResult = null;
                this.unitResult = null;
                this.solution = null;
                this.customSolution = null;
                this.finalResult = null;
                this.fresh.reset();
            }

        }

    }

}
