package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;

/**
 * Interface for parsing, unparsing, and retrieving origin information.
 *
 * @param <T>
 *            Type of the parse result.
 */
public interface ISyntaxService<T> {
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
    public abstract ParseResult<T> parse(String text, FileObject resource, ILanguage language,
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
    public abstract String unparse(T parsed, ILanguage language);


    /**
     * @return Single line comment prefix characters for given language.
     */
    public abstract Iterable<String> singleLineCommentPrefixes(ILanguage language);

    /**
     * @return Multi line comment prefix and postfix characters for given language.
     */
    public abstract Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(ILanguage language);

    /**
     * @return Fence (brackets, parentheses, etc.) open and close characters for given language.
     */
    public abstract Iterable<FenceCharacters> fenceCharacters(ILanguage language);

    /**
     * @return Empty parse result for given resource, language, and optionally a dialect.
     */
    public abstract ParseResult<T>
        emptyParseResult(FileObject resource, ILanguage language, @Nullable ILanguage dialect);
}
