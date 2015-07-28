package org.metaborg.core.transform;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

public interface ITransformerExecutor<ParseT, AnalysisT, TransT> {
    public abstract TransformResult<ParseResult<ParseT>, TransT> transform(ParseResult<ParseT> parseResult,
        IContext context, ITransformerGoal goal) throws TransformerException;

    public abstract TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> transform(
        AnalysisFileResult<ParseT, AnalysisT> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException;
    
    public abstract boolean available(ITransformerGoal goal, IContext context);
}
