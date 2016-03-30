package org.metaborg.core.syntax;

import java.util.Collection;

import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for parsing, unparsing, and retrieving origin information.
 */
public interface ISyntaxService<I extends IInputUnit, P extends IParseUnit> {
    boolean available(ILanguageImpl langImpl);


    P parse(I input) throws ParseException;

    Collection<P> parseAll(Iterable<I> inputs) throws ParseException;


    /**
     * @return Single line comment prefix characters for given language.
     */
    Iterable<String> singleLineCommentPrefixes(ILanguageImpl langImpl);

    /**
     * @return Multi line comment prefix and postfix characters for given language.
     */
    Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(ILanguageImpl langImpl);

    /**
     * @return Fence (brackets, parentheses, etc.) open and close characters for given language.
     */
    Iterable<FenceCharacters> fenceCharacters(ILanguageImpl langImpl);
}
