package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

public interface IParseService<T> {
    /**
     * Parses text, using parsing rules from given language.
     * 
     * @param text
     *            Text to parse.
     * @param resource
     *            Resource associated with the {@code text} to parse.
     * @param language
     *            Language to parse with.
     * @param parserConfig
     *            Parser-specific configuration, or null to use the default configuration.
     * @return Result of parsing.
     * @throws ParseException
     *             when parsing fails unexpectedly.
     */
    public abstract ParseResult<T> parse(String text, FileObject resource, ILanguageImpl language,
        @Nullable IParserConfiguration parserConfig) throws ParseException;

    /**
     * Unparses a parsed fragment back into a string, using unparsing rules from given language.
     * 
     * @param parsed
     *            Parsed fragment.
     * @param language
     *            Language to unparse with.
     * @return Unparsed string.
     */
    public abstract String unparse(T parsed, ILanguageImpl language);

    /**
     * @return Empty parse result for given resource, language, and optionally a dialect.
     */
    public abstract ParseResult<T> emptyParseResult(FileObject resource, ILanguageImpl language,
        @Nullable ILanguageImpl dialect);
}
