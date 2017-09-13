package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nullable;

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
    @Nullable protected final FileObject resource;
    protected final String input;

    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect,
        @Nullable FileObject resource, String input) throws IOException {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.dialect = dialect;
        this.resource = resource;
        this.input = input;
    }
    
    @SuppressWarnings("unchecked")
    protected PT getParseTable(IParseTableTermProvider parseTableTermProvider, ITermFactory termFactory, FileObject grammar) throws IOException {
        if (parseTableTermProvider.getCachedParseTable() == null) {
            PT parseTable = parseTableFromTerm(parseTableTermProvider, termFactory, grammar);
            
            parseTableTermProvider.setCachedParseTable(parseTable);
            
            return parseTable;
        } else {
            return (PT) parseTableTermProvider.getCachedParseTable();
        }
    }
    
    abstract protected PT parseTableFromTerm(IParseTableTermProvider parseTableTermProvider, ITermFactory termFactory, FileObject grammar) throws IOException;

    abstract public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig) throws IOException;

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

    @Nullable public FileObject getResource() {
        return resource;
    }

    public String getInput() {
        return input;
    }
}
