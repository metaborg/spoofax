package org.metaborg.spoofax.core.syntax;

import org.metaborg.sdf2table.parsetable.ParseTable;

public class IncrementalParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;

    private final ParseTable referenceParseTable;


    public IncrementalParserConfig(String startSymbol, IParseTableProvider provider, ParseTable pt) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
        this.referenceParseTable = pt;
    }


    @Override public String getStartSymbol() {
        return this.startSymbol;
    }

    @Override public IParseTableProvider getParseTableProvider() {
        return this.parseTableProvider;
    }

    public ParseTable getReferenceParseTable() {
        return this.referenceParseTable;
    }
}
