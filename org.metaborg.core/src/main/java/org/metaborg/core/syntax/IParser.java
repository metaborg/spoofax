package org.metaborg.core.syntax;

import java.util.Collection;

import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Interface for a context-free parser implementation.
 * 
 * @param <I>
 *            Type of input units.
 * @param <P>
 *            Type of parse units.
 */
public interface IParser<I extends IInputUnit, P extends IParseUnit> {
    /**
     * Parses given input unit into a parse unit.
     * 
     * @param input
     *            Input unit to parse.
     * @return Parse unit.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     * @throws InterruptedException
     *             When parsing is cancelled.
     */
    P parse(I input, IProgress progress, ICancel cancel) throws ParseException, InterruptedException;

    /**
     * Parses all given input units into a parse units.
     * 
     * @param inputs
     *            Input units to parse.
     * @return Parse units.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     * @throws InterruptedException
     *             When parsing is cancelled.
     */
    Collection<P> parseAll(Iterable<I> inputs, IProgress progress, ICancel cancel)
        throws ParseException, InterruptedException;
}
