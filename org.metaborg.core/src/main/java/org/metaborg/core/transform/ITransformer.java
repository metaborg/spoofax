package org.metaborg.core.transform;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for transformations on parsed or analyzed files.
 * 
 * @param <ParseT>
 *            Type of the parse result.
 * @param <AnalysisT>
 *            Type of the analysis result.
 * @param <TransT>
 *            Type of the transformation result.
 */
public interface ITransformer<ParseT, AnalysisT, TransT> {
    /**
     * Transforms given parse result.
     * 
     * @param parseResult
     *            Parse result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param goal
     *            Goal of the transformation.
     * @return Transformed result.
     * @throws TransformerException
     *             When transformation fails.
     */
    public abstract TransformResult<ParseResult<ParseT>, TransT> transform(ParseResult<ParseT> parseResult,
        IContext context, ITransformerGoal goal) throws TransformerException;

    /**
     * Transforms given analysis result.
     * 
     * @param analysisResult
     *            Analysis result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param goal
     *            Goal of the transformation.
     * @return Transformed result.
     * @throws TransformerException
     *             When transformation fails.
     */
    public abstract TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> transform(
        AnalysisFileResult<ParseT, AnalysisT> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException;

    /**
     * Returns if the transformation goal is available in given context.
     * 
     * @param goal
     *            Goal to check for availability.
     * @param context
     *            Context in which to check for availability.
     * @return True if available, false if not.
     * @throws TransformerException
     *             When no executor can be found for given goal.
     */
    public abstract boolean available(ITransformerGoal goal, IContext context);
}
