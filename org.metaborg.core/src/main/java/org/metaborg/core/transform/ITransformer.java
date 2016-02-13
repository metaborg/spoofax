package org.metaborg.core.transform;

import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

public interface ITransformer<P, A, T> {
    TransformResult<P, T> transform(ParseResult<P> input, IContext context,
                                    TransformActionContribution action) throws TransformException;

    TransformResult<A, T> transform(AnalysisFileResult<P, A> input, IContext context,
                                    TransformActionContribution action) throws TransformException;
}
