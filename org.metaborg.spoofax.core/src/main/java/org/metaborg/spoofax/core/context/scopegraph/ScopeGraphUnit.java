package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ScopeGraphUnit implements IScopeGraphUnit {

    private final String source;
    private final ISpoofaxParseUnit parseUnit;

    private final Table<Integer,IStrategoTerm,IStrategoTerm> metadata;

    private @Nullable IStrategoTerm initialResult;
    private @Nullable IStrategoTerm unitResult;
    private @Nullable IStrategoTerm finalResult;

    public ScopeGraphUnit(String source, ISpoofaxParseUnit parseUnit) {
        this.source = source;
        this.parseUnit = parseUnit;
        this.metadata = HashBasedTable.create();
    }


    @Override
    public String source() {
        return source;
    }

    public ISpoofaxParseUnit parseUnit() {
        return parseUnit;
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
    public void setInitialResult(IStrategoTerm result) {
        this.initialResult = result;
    }


    @Override
    public IStrategoTerm initialResult() {
        return initialResult;
    }


    @Override
    public void setUnitResult(IStrategoTerm result) {
        this.unitResult = result;
    }


    @Override
    public IStrategoTerm unitResult() {
        return unitResult;
    }

    @Override
    public void setFinalResult(IStrategoTerm result) {
        this.finalResult = result;
    }


    @Override
    public IStrategoTerm finalResult() {
        return finalResult;
    }


    @Override
    public void reset() {
        metadata.clear();
        initialResult = null;
        unitResult = null;
        finalResult = null;
    }
 
}
