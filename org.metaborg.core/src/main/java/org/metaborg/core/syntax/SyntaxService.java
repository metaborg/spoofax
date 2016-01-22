package org.metaborg.core.syntax;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;

import com.google.inject.Inject;

public abstract class SyntaxService<T> implements ISyntaxService<T> {
    private final Map<String, IParseService<T>> parsers;


    @Inject public SyntaxService(Map<String, IParseService<T>> parsers) {
        this.parsers = parsers;
    }


    @Override public ParseResult<T> parse(String text, @Nullable FileObject resource, ILanguageImpl language,
        IParserConfiguration parserConfig) throws ParseException {
        final IParseService<T> parser = parser(language);
        if(parser == null) {
            throw new ParseException(resource, language, "No parser");
        }
        return parser.parse(text, resource, language, parserConfig);
    }

    @Override public String unparse(T parsed, ILanguageImpl language) {
        final IParseService<T> parser = parser(language);
        if(parser == null) {
            // TODO: better exception
            throw new MetaborgRuntimeException("No parser");
        }
        return parser.unparse(parsed, language);
    }

    @Override public ParseResult<T>
        emptyParseResult(FileObject resource, ILanguageImpl language, @Nullable ILanguageImpl dialect) {
        final IParseService<T> parser = parser(language);
        if(parser == null) {
            // TODO: better exception
            throw new MetaborgRuntimeException("No parser");
        }
        return parser.emptyParseResult(resource, language, dialect);
    }


    private @Nullable IParseService<T> parser(ILanguageImpl language) {
        final ParseFacet facet = language.facet(ParseFacet.class);
        if(facet == null) {
            return null;
        }
        return parsers.get(facet.type);
    }
}
