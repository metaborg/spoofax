package org.metaborg.spoofax.core.syntax;

public class ParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableTermProvider parseTableProvider;


    public ParserConfig(String startSymbol, IParseTableTermProvider provider) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
    }


    @Override public String getStartSymbol() {
        return this.startSymbol;
    }

    @Override public IParseTableTermProvider getParseTableProvider() {
        return this.parseTableProvider;
    }
}
