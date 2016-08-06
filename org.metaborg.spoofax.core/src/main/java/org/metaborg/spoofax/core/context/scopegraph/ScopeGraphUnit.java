package org.metaborg.spoofax.core.context.scopegraph;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ScopeGraphUnit implements IScopeGraphUnit, Serializable {

    private static final long serialVersionUID = 2505224489843232373L;

    private final String source;

    private final Table<Integer,IStrategoTerm,IStrategoTerm> metadata;

    private @Nullable IStrategoTerm initial;
    private @Nullable IStrategoTerm result;

    public ScopeGraphUnit(String source) {
        this.source = source;
        this.metadata = HashBasedTable.create();
    }


    @Override
    public String source() {
        return source;
    }


    @Override
    public void setMetadata(int nodeId, IStrategoTerm key, IStrategoTerm value) {
        metadata.put(nodeId, key, value);
    }

    @Override
    public IStrategoTerm metadata(int nodeId, IStrategoTerm key) {
        return metadata.get(nodeId, key);
    }


    @Override
    public void setInitial(IStrategoTerm result) {
        this.initial = result;
    }

    @Override
    public IStrategoTerm initial() {
        return initial;
    }

    @Override
    public void setResult(IStrategoTerm result) {
        this.result = result;
    }

    @Override
    public IStrategoTerm result() {
        return result;
    }


    @Override
    public void reset() {
        metadata.clear();
        initial = null;
        result = null;
    }
 
}
