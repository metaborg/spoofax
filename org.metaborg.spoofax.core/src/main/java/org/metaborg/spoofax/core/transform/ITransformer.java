package org.metaborg.spoofax.core.transform;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;

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
     * @param transformer
     *            Name of the transformer to apply.
     * @return Transformed result.
     * @throws TransformerException
     *             when transformation fails.
     */
    public abstract TransformResult<ParseResult<ParseT>, TransT> transformParsed(ParseResult<ParseT> parseResult,
        IContext context, String transformer) throws TransformerException;

    /**
     * Transforms given analysis result.
     * 
     * @param analysisResult
     *            Analysis result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param transformer
     *            Name of the transformer to apply.
     * @return Transformed result.
     * @throws TransformerException
     *             when transformation fails.
     */
    public abstract TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> transformAnalyzed(
        AnalysisFileResult<ParseT, AnalysisT> analysisResult, IContext context, String transformer)
        throws TransformerException;
}
