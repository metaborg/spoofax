package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for parsing, unparsing, and retrieving origin information.
 *
 * @param <P>
 *            Type of the parse result.
 */
public interface ISyntaxService<P> {
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
    ParseResult<P> parse(String text, FileObject resource, ILanguageImpl language,
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
    String unparse(P parsed, ILanguageImpl language);


    /**
     * @return Single line comment prefix characters for given language.
     */
    Iterable<String> singleLineCommentPrefixes(ILanguageImpl language);

    /**
     * @return Multi line comment prefix and postfix characters for given language.
     */
    Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(ILanguageImpl language);

    /**
     * @return Fence (brackets, parentheses, etc.) open and close characters for given language.
     */
    Iterable<FenceCharacters> fenceCharacters(ILanguageImpl language);

    /**
     * @return Empty parse result for given resource, language, and optionally a dialect.
     */
    ParseResult<P>
        emptyParseResult(FileObject resource, ILanguageImpl language, @Nullable ILanguageImpl dialect);
}
