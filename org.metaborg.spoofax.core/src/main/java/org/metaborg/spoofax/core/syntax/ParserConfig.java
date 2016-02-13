package org.metaborg.spoofax.core.syntax;

public class ParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;


    public ParserConfig(String startSymbol, IParseTableProvider provider) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
    }


    @Override public String getStartSymbol() {
        return this.startSymbol;
    }

    @Override public IParseTableProvider getParseTableProvider() {
        return this.parseTableProvider;
    }
}
