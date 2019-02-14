package org.metaborg.spoofax.core.syntax;

public class ParserConfig implements IParserConfig {
    private final String startSymbol;
    private final IParseTableProvider parseTableProvider;
    private final ImploderImplementation imploder;


    public ParserConfig(String startSymbol, IParseTableProvider provider, ImploderImplementation imploder) {
        this.startSymbol = startSymbol;
        this.parseTableProvider = provider;
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
}
