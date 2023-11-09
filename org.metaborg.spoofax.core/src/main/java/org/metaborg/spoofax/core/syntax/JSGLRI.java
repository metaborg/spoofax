package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Set;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.shared.BadTokenException;

abstract public class JSGLRI<PT> {
    protected final IParserConfig config;
    protected final ITermFactory termFactory;
    protected final ILanguageImpl language;
    protected final ILanguageImpl dialect;

    protected PT parseTable;

    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect) {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.dialect = dialect;
    }

    @SuppressWarnings("unchecked") protected PT getParseTable(IParseTableProvider parseTableProvider)
        throws IOException {
        // Since JSGLR v1 and v2 use different parse table representations we have to cast here
        return (PT) parseTableProvider.parseTable();
    }

    abstract public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig, @Nullable FileObject resource,
        String input);

    abstract public Set<BadTokenException> getCollectedErrors();

    protected String getOrDefaultStartSymbol(@Nullable JSGLRParserConfiguration parserConfig) {
        if(parserConfig != null && parserConfig.overridingStartSymbol != null) {
            return parserConfig.overridingStartSymbol;
        } else {
            return config.getStartSymbol();
        }
    }

    public IParserConfig getConfig() {
        return config;
    }

    public ILanguageImpl getLanguage() {
        return language;
    }

    public ILanguageImpl getDialect() {
        return dialect;
    }

}
