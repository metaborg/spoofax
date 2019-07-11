package org.metaborg.spoofax.core.syntax;

import org.metaborg.parsetable.IParseTable;

public class IncrementalParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;
    private final ImploderImplementation imploder;

    private final IParseTable referenceParseTable;


    public IncrementalParserConfig(String startSymbol, IParseTableProvider provider, IParseTable pt,
        ImploderImplementation imploder) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
        this.referenceParseTable = pt;
        this.imploder = imploder;
    }


    @Override public String getStartSymbol() {
        return this.startSymbol;
    }

    @Override public IParseTableProvider getParseTableProvider() {
        return this.parseTableProvider;
    }

    @Override public ImploderImplementation getImploderSetting() {
        return this.imploder;
    }

    public IParseTable getReferenceParseTable() {
        return this.referenceParseTable;
    }
}
