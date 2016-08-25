package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

class ScopeGraphUnit implements ISpoofaxScopeGraphUnit, Serializable {

    private static final long serialVersionUID = 2505224489843232373L;

    private final String source;

    private Table<Integer,IStrategoTerm,IStrategoTerm> rawData;
    private Table<Integer,IStrategoTerm,IStrategoTerm> finalData;

    private @Nullable IStrategoTerm initial;
    private @Nullable IScopeGraph scopeGraph;
    private @Nullable INameResolution nameResolution;
    private @Nullable IStrategoTerm analysis;

    public ScopeGraphUnit(String source) {
        this.source = source;
        this.rawData = HashBasedTable.create();
        this.finalData = HashBasedTable.create();
    }


    @Override
    public String source() {
        return source;
    }


    @Override
    public void setMetadata(int nodeId, IStrategoTerm key, IStrategoTerm value) {
        rawData.put(nodeId, key, value);
    }

    public Table<Integer,IStrategoTerm,IStrategoTerm> processRawData() {
        finalData.clear();
        finalData.putAll(rawData);
        return finalData;
    }
    
    @Override
    public IStrategoTerm metadata(int nodeId, IStrategoTerm key) {
        return finalData.get(nodeId, key);
    }


    public void setInitial(IStrategoTerm result) {
        this.initial = result;
    }

    public IStrategoTerm initial() {
        return initial;
    }

    public void setAnalysis(IStrategoTerm analysis) {
        this.analysis = analysis;
    }

    @Override
    public IStrategoTerm analysis() {
        return analysis;
    }


    @Override
    public IScopeGraph scopeGraph() {
        return scopeGraph;
    }

    public void setScopeGraph(IScopeGraph scopeGraph) {
        this.scopeGraph = scopeGraph;
    }

    @Override
    public INameResolution nameResolution() {
        return nameResolution;
    }

    public void setNameResolution(INameResolution nameResolution) {
        this.nameResolution = nameResolution;
    }
 
    
    public void reset() {
        rawData.clear();
        finalData.clear();
        initial = null;
        scopeGraph = null;
        nameResolution = null;
        analysis = null;
    }
    
}