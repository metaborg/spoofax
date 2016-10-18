package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.nabl2.context.IScopeGraphUnit;
import org.metaborg.nabl2.solution.INameResolution;
import org.metaborg.nabl2.solution.IScopeGraph;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface ISingleFileScopeGraphUnit extends IScopeGraphUnit {

    void setAnalysis(IStrategoTerm analysis);

    void setScopeGraph(IScopeGraph scopeGraph);

    void setNameResolution(INameResolution nameResolution);

    void clear();

}