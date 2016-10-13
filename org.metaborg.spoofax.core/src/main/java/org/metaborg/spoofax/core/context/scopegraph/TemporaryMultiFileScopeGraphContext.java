package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TemporaryMultiFileScopeGraphContext extends AbstractTemporaryScopeGraphContext<IMultiFileScopeGraphUnit>
        implements IMultiFileScopeGraphContext {

    private final IMultiFileScopeGraphContext context;

    public TemporaryMultiFileScopeGraphContext(IMultiFileScopeGraphContext context) {
        super(context);
        this.context = context;
    }

    @Override public void setScopeGraph(IScopeGraph scopeGraph) {
        context.setScopeGraph(scopeGraph);
    }

    @Override public void setNameResolution(INameResolution nameResolution) {
        context.setNameResolution(nameResolution);
    }

    @Override public void setAnalysis(IStrategoTerm analysis) {
        context.setAnalysis(analysis);
    }

    @Override public void clear() {
        context.clear();
    }

}