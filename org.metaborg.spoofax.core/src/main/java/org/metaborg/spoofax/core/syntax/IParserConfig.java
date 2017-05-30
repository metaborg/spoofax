package org.metaborg.spoofax.core.syntax;

public interface IParserConfig {
    String getStartSymbol();

    IParseTableTermProvider getParseTableProvider();
}
