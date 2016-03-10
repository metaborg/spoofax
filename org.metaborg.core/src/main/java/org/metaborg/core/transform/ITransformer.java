package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for transformation implementations.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <TP>
 *            Type of transform units with parse units as input.
 * @param <TA>
 *            Type of transform units with analyze units as input.
 */
public interface ITransformer<P extends IParseUnit, A extends IAnalyzeUnit, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>> {
    TP transform(P input, IContext context, TransformActionContrib action) throws TransformException;

    TA transform(A input, IContext context, TransformActionContrib action) throws TransformException;

    Collection<TP> transformAllParsed(Iterable<P> inputs, IContext context, TransformActionContrib action)
        throws TransformException;

    Collection<TA> transformAllAnalyzed(Iterable<A> inputs, IContext context, TransformActionContrib action)
        throws TransformException;
}
