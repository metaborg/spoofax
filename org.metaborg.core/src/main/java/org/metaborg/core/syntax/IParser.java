package org.metaborg.core.syntax;

import java.util.Collection;

public interface IParser<I extends IInputUnit, P extends IParseUnit> {
    P parse(I input) throws ParseException;

    Collection<P> parseAll(Iterable<I> inputs) throws ParseException;
}
