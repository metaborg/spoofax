package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.scopegraph.impl.ASTMetadata;
import org.metaborg.scopegraph.impl.OccurrenceTypes;
import org.metaborg.spoofax.core.context.scopegraph.SingleFileScopeGraphContext.State;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class SingleFileScopeGraphContext extends AbstractScopeGraphContext<State>
        implements ISingleFileScopeGraphContext {

    public SingleFileScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        super(injector, identifier);
    }

    @Override protected State initState() {
        return new State();
    }

    @Override public ISingleFileScopeGraphUnit unit(String resource) {
        ISingleFileScopeGraphUnit unit;
        if ((unit = state.units.get(resource)) == null) {
            state.units.put(resource, (unit = state.new Unit(resource)));
        }
        return unit;
    }

    @Override public Collection<ISingleFileScopeGraphUnit> units() {
        return state.units.values();
    }

    @Override public void removeUnit(String resource) {
        state.units.remove(resource);
    }

    static class State implements Serializable {

        private static final long serialVersionUID = -8878117069378041686L;

        final Map<String,ISingleFileScopeGraphUnit> units = Maps.newHashMap();

        class Unit implements ISingleFileScopeGraphUnit {

            private static final long serialVersionUID = -2828933828253182233L;

            private final String resource;

            private @Nullable IScopeGraph scopeGraph;
            private @Nullable INameResolution nameResolution;
            private @Nullable ASTMetadata astMetadata;
            private @Nullable OccurrenceTypes occurrenceTypes;
            private @Nullable IStrategoTerm analysis;

            private Unit(String resource) {
                this.resource = resource;
            }

            @Override public String resource() {
                return resource;
            }

            @Override public IStrategoTerm partialAnalysis() {
                return null;
            }

            @Override public IScopeGraph scopeGraph() {
                return scopeGraph;
            }

            @Override public INameResolution nameResolution() {
                return nameResolution;
            }

            @Override public ASTMetadata astMetadata() {
                return astMetadata;
            }

            @Override public OccurrenceTypes occurrenceTypes() {
                return occurrenceTypes;
            }

            @Override public IStrategoTerm analysis() {
                return analysis;
            }

            @Override public void setAnalysis(IStrategoTerm analysis) {
                this.analysis = analysis;
            }

            @Override public void setScopeGraph(IScopeGraph scopeGraph) {
                this.scopeGraph = scopeGraph;
            }

            @Override public void setNameResolution(INameResolution nameResolution) {
                this.nameResolution = nameResolution;
            }

            @Override public void setAstMetadata(ASTMetadata astMetadata) {
                this.astMetadata = astMetadata;
            }

            @Override public void setOccurrenceTypes(OccurrenceTypes occurrenceTypes) {
                this.occurrenceTypes = occurrenceTypes;
            }

            @Override public void clear() {
                this.scopeGraph = null;
                this.nameResolution = null;
                this.analysis = null;
            }

        }

    }

}