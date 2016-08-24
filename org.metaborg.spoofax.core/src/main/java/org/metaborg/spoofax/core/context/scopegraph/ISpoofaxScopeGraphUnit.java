package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Table;

public interface ISpoofaxScopeGraphUnit extends IScopeGraphUnit {

    void setInitial(IStrategoTerm solution);
    IStrategoTerm initial();

    void setScopeGraph(IScopeGraph scopeGraph);

    void setNameResolution(INameResolution nameResolution);

    void setAnalysis(IStrategoTerm analysis);

    Table<Integer, IStrategoTerm, IStrategoTerm> processRawData();

    void reset();

}
