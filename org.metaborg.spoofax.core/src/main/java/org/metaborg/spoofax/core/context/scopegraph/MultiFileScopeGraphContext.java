package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.spoofax.core.context.scopegraph.MultiFileScopeGraphContext.State;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
        if ((unit = state.units.get(resource)) == null) {
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

    @Override public void setScopeGraph(IScopeGraph scopeGraph) {
        state.scopeGraph = scopeGraph;
    }

    @Override public void setNameResolution(INameResolution nameResolution) {
        state.nameResolution = nameResolution;
    }

    @Override public void setAnalysis(IStrategoTerm analysis) {
        state.analysis = analysis;
    }

    @Override public void clear() {
        state.clear();
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8133657561476824164L;

        final Map<String,IMultiFileScopeGraphUnit> units = Maps.newHashMap();

        @Nullable IStrategoTerm analysis;
        @Nullable INameResolution nameResolution;
        @Nullable IScopeGraph scopeGraph;

        public void clear() {
            analysis = null;
            nameResolution = null;
            scopeGraph = null;
        }

        class Unit implements IMultiFileScopeGraphUnit {

            private static final long serialVersionUID = 1176844388074495439L;

            private final String resource;

            private @Nullable IStrategoTerm partialAnalysis;

            private Unit(String resource) {
                this.resource = resource;
            }

            @Override public String resource() {
                return resource;
            }

            @Override public IStrategoTerm partialAnalysis() {
                return partialAnalysis;
            }

            @Override public IScopeGraph scopeGraph() {
                return scopeGraph;
            }

            @Override public INameResolution nameResolution() {
                return nameResolution;
            }

            @Override public IStrategoTerm analysis() {
                return analysis;
            }

            @Override public void setPartialAnalysis(IStrategoTerm partialAnalysis) {
                this.partialAnalysis = partialAnalysis;
            }

            @Override public void reset() {
                this.partialAnalysis = null;
            }

        }

    }

}