package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.ITransformAction;
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
 * @param <TUP>
 *            Type of transform units with parse units as input.
 * @param <TUA>
 *            Type of transform units with analyze units as input.
 */
public interface ITransformer<P extends IParseUnit, A extends IAnalyzeUnit, TUP extends ITransformUnit<P>, TUA extends ITransformUnit<A>, TA extends ITransformAction> {
    TUP transform(P input, IContext context, TransformActionContrib<TA> action, ITransformConfig config)
        throws TransformException;

    TUA transform(A input, IContext context, TransformActionContrib<TA> action, ITransformConfig config)
        throws TransformException;

    Collection<TUP> transformAllParsed(Iterable<P> inputs, IContext context, TransformActionContrib<TA> action,
        ITransformConfig config) throws TransformException;

    Collection<TUA> transformAllAnalyzed(Iterable<A> inputs, IContext context, TransformActionContrib<TA> action,
        ITransformConfig config) throws TransformException;
}
