package org.metaborg.core.syntax;

import java.util.Collection;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.processing.NullCancel;
import org.metaborg.core.processing.NullProgress;
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
     * @throws InterruptedException
     *             When parsing is cancelled.
     */
    P parse(I input, IProgress progress, ICancel cancel) throws ParseException, InterruptedException;

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
        try {
            return parse(input, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
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
     * @throws InterruptedException
     *             When parsing is cancelled.
     */
    Collection<P> parseAll(Iterable<I> inputs, IProgress progress, ICancel cancel)
        throws ParseException, InterruptedException;

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
        try {
            return parseAll(inputs, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
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
