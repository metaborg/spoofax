package org.metaborg.spoofax.core.transform;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;

public interface ITransformerExecutor<ParseT, AnalysisT, TransT> {
    public abstract TransformResult<ParseResult<ParseT>, TransT> transform(ParseResult<ParseT> parseResult,
        IContext context, ITransformerGoal goal) throws TransformerException;

    public abstract TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> transform(
        AnalysisFileResult<ParseT, AnalysisT> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException;
    
    public abstract boolean available(ITransformerGoal goal, IContext context);
}
