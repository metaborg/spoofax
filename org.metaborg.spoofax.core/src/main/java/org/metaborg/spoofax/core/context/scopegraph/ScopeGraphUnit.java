package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ScopeGraphUnit implements IScopeGraphUnit {

    private final String source;
    private final ISpoofaxParseUnit parseUnit;

    private IStrategoTerm constraint;
    private final Table<IStrategoTerm,IStrategoTerm,IStrategoTerm> metadata;
    private @Nullable IStrategoTerm analysis;

    public ScopeGraphUnit(String source, ISpoofaxParseUnit parseUnit) {
        this.source = source;
        this.metadata = HashBasedTable.create();
        this.parseUnit = parseUnit;
    }


    @Override
    public String source() {
        return source;
    }

    public ISpoofaxParseUnit parseUnit() {
        return parseUnit;
    }


    @Override
    public void setConstraint(IStrategoTerm constraint) {
        this.constraint = constraint;
    }

    @Override
    public IStrategoTerm constraint() {
        return constraint;
    }


    @Override
    public void setMetadata(IStrategoTerm node, IStrategoTerm key, IStrategoTerm value) {
        metadata.put(node, key, value);
    }

    @Override
    public IStrategoTerm metadata(IStrategoTerm node, IStrategoTerm key) {
        return metadata.get(node, key);
    }


    @Override
    public void setAnalysis(IStrategoTerm analysis) {
        this.analysis = analysis;
    }

    @Override
    public IStrategoTerm analysis() {
        return analysis;
    }


    @Override
    public void reset() {
        constraint = null;
        metadata.clear();
        analysis = null;
    }
 
}
