package org.metaborg.core.transform;

import org.metaborg.core.action.ITransformGoal;
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
     * Transforms given result with given goal.
     * 
     * @param input
     *            Result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param goal
     *            Transform goal to execute.
     * @return Transformation results. Multiple results are returned because a goal can result in multiple
     *         transformations being executed, e.g. multiple compiler actions.
     * @throws TransformException
     *             When transformation fails.
     */
    TransformResults<P, T> transform(ParseResult<P> input, IContext context, ITransformGoal goal)
        throws TransformException;

    /**
     * Transforms given result with given goal.
     * 
     * @param input
     *            Result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param goal
     *            Transform goal to execute.
     * @return Transformation results. Multiple results are returned because a goal can result in multiple
     *         transformations being executed, e.g. multiple compiler actions.
     * @throws TransformException
     *             When transformation fails.
     */
    TransformResults<A, T> transform(AnalysisFileResult<P, A> input, IContext context,
                                     ITransformGoal goal) throws TransformException;

    /**
     * Checks if transform actions are available for given context and goal.
     * 
     * @param context
     *            Context to check transform actions from.
     * @param goal
     *            Goal to check transform actions for.
     * @return True if transform actions are available, false if not.
     */
    boolean available(IContext context, ITransformGoal goal);

    /**
     * Checks if analysis is required for given context and goal.
     * 
     * @param context
     *            Context to check from.
     * @param goal
     *            Goal to check for.
     * @return True if analysis is required, false if not.
     */
    boolean requiresAnalysis(IContext context, ITransformGoal goal);


    /**
     * Transforms given result with given action.
     * 
     * @param input
     *            Result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param action
     *            Transform action to execute.
     * @return Transform result.
     * @throws TransformException
     *             When transformation fails.
     * @throws TransformException
     *             When transformation action requires analysis.
     */
    TransformResult<P, T> transformAction(ParseResult<P> input, IContext context,
                                          TransformActionContribution action) throws TransformException;

    /**
     * Transforms given result with given action.
     * 
     * @param input
     *            Result to transform.
     * @param context
     *            Context in which to apply transformation.
     * @param action
     *            Transform action to execute.
     * @return Transform result.
     * @throws TransformException
     *             When transformation fails.
     */
    TransformResult<A, T> transformAction(AnalysisFileResult<P, A> input, IContext context,
                                          TransformActionContribution action) throws TransformException;
}
