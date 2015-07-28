package org.metaborg.spoofax.core.syntax;

public interface IParserConfig {
    public String getStartSymbol();

    public IParseTableProvider getParseTableProvider();
}
