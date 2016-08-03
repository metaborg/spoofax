package org.metaborg.core.syntax;

import java.util.Collection;

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
     */
    P parse(I input) throws ParseException;

    /**
     * Parses all given input units into a parse units.
     * 
     * @param inputs
     *            Input units to parse.
     * @return Parse units.
     * @throws ParseException
     *             When parsing fails unexpectedly.
     */
    Collection<P> parseAll(Iterable<I> inputs) throws ParseException;
}
