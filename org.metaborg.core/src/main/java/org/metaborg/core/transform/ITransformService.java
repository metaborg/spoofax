package org.metaborg.core.transform;

import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for transformations on parsed or analyzed files.
 * 
 * @param <P>
 *            Type of the parse result.
 * @param <A>
 *            Type of the analysis result.
 * @param <T>
 *            Type of the transformation result.
 */
public interface ITransformService<P, A, T> {
    /**
     * Transforms given parse result.
     * 
     * @param input
     *            Parse result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param action
     *            Transform action to execute.
     * @return Transformed result.
     * @throws TransformException
     *             When transformation fails.
     */
    public abstract TransformResult<ParseResult<P>, T> transform(ParseResult<P> input, IContext context,
        TransformActionContribution action) throws TransformException;

    /**
     * Transforms given analysis result.
     * 
     * @param input
     *            Analysis result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param action
     *            Transform action to execute.
     * @return Transformed result.
     * @throws TransformException
     *             When transformation fails.
     */
    public abstract TransformResult<AnalysisFileResult<P, A>, T> transform(AnalysisFileResult<P, A> input,
        IContext context, TransformActionContribution action) throws TransformException;
}
