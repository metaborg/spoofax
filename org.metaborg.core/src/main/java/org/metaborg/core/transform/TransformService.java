package org.metaborg.core.transform;

import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

import com.google.inject.Inject;

public class TransformService<P, A, T> implements ITransformService<P, A, T> {
    private final ITransformer<P, A, T> transformer;


    @Inject public TransformService(ITransformer<P, A, T> transformer) {
        this.transformer = transformer;
    }


    @Override public TransformResult<ParseResult<P>, T> transform(ParseResult<P> result, IContext context,
        TransformActionContribution action) throws TransformException {
        return transformer.transform(result, context, action);
    }

    @Override public TransformResult<AnalysisFileResult<P, A>, T> transform(AnalysisFileResult<P, A> result,
        IContext context, TransformActionContribution action) throws TransformException {
        return transformer.transform(result, context, action);
    }
}
