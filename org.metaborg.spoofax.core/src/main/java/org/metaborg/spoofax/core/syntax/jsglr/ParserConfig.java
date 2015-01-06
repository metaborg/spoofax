package org.metaborg.spoofax.core.syntax.jsglr;



public class ParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;
    private final int timeout;


    public ParserConfig(String startSymbol, IParseTableProvider provider, int timeout) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
        this.timeout = timeout;
    }


    @Override public String getStartSymbol() {
        return this.startSymbol;
    }

    @Override public IParseTableProvider getParseTableProvider() {
        return this.parseTableProvider;
    }

    @Override public int getTimeout() {
        return this.timeout;
    }
}
