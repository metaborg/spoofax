package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.nabl2.solution.INameResolution;
import org.metaborg.nabl2.solution.IScopeGraph;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IMultiFileScopeGraphContext extends ISpoofaxScopeGraphContext<IMultiFileScopeGraphUnit> {

    void setScopeGraph(IScopeGraph scopeGraph);

    void setNameResolution(INameResolution nameResolution);

    void setAnalysis(IStrategoTerm analysis);

    void clear();

}