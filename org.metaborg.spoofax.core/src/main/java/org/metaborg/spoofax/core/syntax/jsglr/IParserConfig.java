package org.metaborg.spoofax.core.syntax.jsglr;

public interface IParserConfig {
    public String getStartSymbol();

    public IParseTableProvider getParseTableProvider();
}
