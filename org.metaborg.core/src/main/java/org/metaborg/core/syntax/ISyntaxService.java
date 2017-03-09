package org.metaborg.core.syntax;

import java.util.Collection;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.processing.NullCancellationToken;
import org.metaborg.core.processing.NullProgressReporter;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Interface for context-free syntactical services, including parsing and information about lexical characters.
 * 
 * @param <I>
 *            Type of input units.
 * @param <P>
 *            Type of parse units.
 */
public interface ISyntaxService<I extends IInputUnit, P extends IParseUnit> {
    /**
     * Checks if syntactical services are available for given language implementation.
     * 
     * @param langImpl
     *            Language implementation to check.
     * @return True if syntactical services are available, false if not.
     */
    boolean available(ILanguageImpl langImpl);


    /**
     * Parses given input unit into a parse unit.
     * 
     * @param input
     *            Input unit to parse.
     * @param progress
     *            Progress reporter.
     * @param cancel
     *            Cancellation token.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    P parse(I input, IProgress progress, ICancel cancel) throws ParseException;

    /**
     * Parses given input unit into a parse unit.
     * 
     * @param input
     *            Input unit to parse.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    default P parse(I input) throws ParseException {
        return parse(input, new NullProgressReporter(), new NullCancellationToken());
    }

    /**
     * Parses all given input units into a parse units.
     * 
     * @param inputs
     *            Input units to parse.
     * @param progress
     *            Progress reporter.
     * @param cancel
     *            Cancellation token.
     * @return Parse units.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    Collection<P> parseAll(Iterable<I> inputs, IProgress progress, ICancel cancel) throws ParseException;

    /**
     * Parses all given input units into a parse units.
     * 
     * @param inputs
     *            Input units to parse.
     * @return Parse units.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    default Collection<P> parseAll(Iterable<I> inputs) throws ParseException {
        return parseAll(inputs, new NullProgressReporter(), new NullCancellationToken());
    }


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
