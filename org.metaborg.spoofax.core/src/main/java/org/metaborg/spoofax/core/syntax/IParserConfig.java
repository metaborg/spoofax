package org.metaborg.spoofax.core.syntax;

public interface IParserConfig {
    String getStartSymbol();

    IParseTableProvider getParseTableProvider();

    ImploderImplementation getImploderSetting();
}
