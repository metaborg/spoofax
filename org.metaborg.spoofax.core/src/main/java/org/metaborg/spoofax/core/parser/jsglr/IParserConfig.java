package org.metaborg.spoofax.core.parser.jsglr;


public interface IParserConfig {
    public String getStartSymbol();

    public IParseTableProvider getParseTableProvider();

    public int getTimeout();
}
