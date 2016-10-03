package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.nabl2.context.IScopeGraphUnit;
import org.metaborg.nabl2.solution.INameResolution;
import org.metaborg.nabl2.solution.IScopeGraph;
import org.metaborg.solver.constraints.IConstraint;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Table;

public interface ISpoofaxScopeGraphUnit extends IScopeGraphUnit {

    void setInitial(IStrategoTerm solution);

    IStrategoTerm initial();

    void setScopeGraph(IScopeGraph scopeGraph);

    void setNameResolution(INameResolution nameResolution);

    void setConstraint(IConstraint constraint);

    void setAnalysis(IStrategoTerm analysis);

    Table<Integer,IStrategoTerm,IStrategoTerm> processRawData();

    void reset();

}
