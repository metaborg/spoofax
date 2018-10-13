package org.metaborg.spoofax.core.syntax;

import org.metaborg.sdf2table.parsetable.ParseTable;

public class IncrementalParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;
    private final ImploderImplementation imploder;

    private final ParseTable referenceParseTable;


    public IncrementalParserConfig(String startSymbol, IParseTableProvider provider, ParseTable pt,
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

    public ParseTable getReferenceParseTable() {
        return this.referenceParseTable;
    }
}
