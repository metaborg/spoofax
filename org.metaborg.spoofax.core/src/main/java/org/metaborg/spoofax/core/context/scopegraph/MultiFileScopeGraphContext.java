package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.Fresh;
import org.metaborg.meta.nabl2.solver.PartialSolution;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.spoofax.core.context.scopegraph.MultiFileScopeGraphContext.State;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

public class MultiFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements IMultiFileScopeGraphContext {

    public MultiFileScopeGraphContext(Injector injector, ContextIdentifier identifier, NaBL2Config config) {
        super(injector, identifier, config);
    }

    @Override protected State initState() {
        return new State();
    }

    @Override public IMultiFileScopeGraphUnit unit(String resource) {
        resource = normalizeResource(resource);
        IMultiFileScopeGraphUnit unit;
        boolean isProject = false;
        try {
            isProject = location().resolveFile(resource).getName().equals(location().getName());
        } catch(FileSystemException e) {
        }
        if((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource, isProject)));
        }
        return unit;
    }

    @Override public void removeUnit(String resource) {
        resource = normalizeResource(resource);
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

        final Map<String, IMultiFileScopeGraphUnit> units = Maps.newHashMap();

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
            private final boolean isProject;
            private final Fresh fresh;

            private UnitResult unitResult;
            private PartialSolution partialSolution;

            private Unit(String resource, boolean isProject) {
                this.resource = resource;
                this.isProject = isProject;
                this.fresh = new Fresh();
                clear();
            }

            @Override public String resource() {
                return resource;
            }

            @Override public void setUnitResult(UnitResult result) {
                this.unitResult = result;
            }

            @Override public Optional<UnitResult> unitResult() {
                return Optional.ofNullable(unitResult);
            }

            @Override public Set<IConstraint> constraints() {
                final Set<IConstraint> constraints = Sets.newHashSet();
                if(isProject) {
                    Optional.ofNullable(initialResult).map(ir -> ir.getConstraints()).ifPresent(constraints::addAll);
                } else {
                    unitResult().map(ur -> ur.getConstraints()).ifPresent(constraints::addAll);
                }
                return constraints;
            }

            @Override public void setPartialSolution(PartialSolution constraints) {
                this.partialSolution = constraints;
            }

            @Override public Optional<PartialSolution> partialSolution() {
                return Optional.ofNullable(partialSolution);
            }

            @Override public Optional<Solution> solution() {
                return Optional.ofNullable(solution);
            }

            @Override public Optional<CustomSolution> customSolution() {
                return Optional.ofNullable(customSolution);
            }

            @Override public Fresh fresh() {
                return fresh;
            }

            @Override public boolean isPrimary() {
                return isProject;
            }

            @Override public void clear() {
                this.unitResult = null;
                this.fresh.reset();
            }

        }

    }

}
