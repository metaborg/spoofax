package org.metaborg.core.transform;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

public interface ITransformerExecutor<P, A, T> {
    public abstract TransformResult<ParseResult<P>, T> transform(ParseResult<P> parseResult, IContext context,
        ITransformerGoal goal) throws TransformerException;

    public abstract TransformResult<AnalysisFileResult<P, A>, T> transform(AnalysisFileResult<P, A> analysisResult,
        IContext context, ITransformerGoal goal) throws TransformerException;

    public abstract boolean available(ITransformerGoal goal, IContext context);
}
