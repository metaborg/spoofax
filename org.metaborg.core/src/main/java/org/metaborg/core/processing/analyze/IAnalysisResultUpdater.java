package org.metaborg.core.processing.analyze;

import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for updating analysis results which are requested by an {@link IAnalysisResultRequester}.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 */
public interface IAnalysisResultUpdater<P, A> {
    /**
     * Invalidates the analysis result for given resource. Must be followed by a call to {@link #update} or
     * {@link #error} for that resource eventually. Failing to do so will block any request made while resource was in
     * an invalid state.
     * 
     * @param resource
     *            Resource to invalidate.
     */
    void invalidate(FileObject resource);

    /**
     * Invalidates the analysis results for sources in given parse results. Must be followed by a call to
     * {@link #update} or {@link #error} for those resources eventually. Failing to do so will block any requests made
     * while resources were in invalid states.
     * 
     * 
     * @param parseResults
     *            Parse results with sources to invalidate.
     */
    void invalidate(Iterable<ParseResult<P>> parseResults);

    /**
     * Updates the analysis result for a single resource. Pushes the analysis result to subscribed requests.
     * 
     * @param result
     *            Result to update.
     * @param parentResult
     *            Parent of the result to update.
     * @param removedResources
     *            Set of resources that have actually been removed instead of updated. Used for legacy analysis where
     *            removal is indicated by an empty tuple as parse result.
     */
    void update(AnalysisFileResult<P, A> result, AnalysisResult<P, A> parentResult,
                Set<FileName> removedResources);

    /**
     * Updates the analysis results for resources in given result. Pushes analysis results to subscribed requests.
     * Removes cached analysis results for given removed resource.
     * 
     * @param parentResult
     *            Parent of the results to update.
     * @param removedResources
     *            Set of resources that have actually been removed instead of updated. Used for legacy analysis where
     *            removal is indicated by an empty tuple as parse result.
     */
    void update(AnalysisResult<P, A> parentResult, Set<FileName> removedResources);

    /**
     * Updates the analysis results for resources in given result. Pushes analysis results to subscribed requests.
     * 
     * @param parentResult
     *            Parent of the results to update.
     */
    void update(AnalysisResult<P, A> parentResult);

    /**
     * Sets an analysis error for given resource. Pushes the analysis error to subscribed requests.
     * 
     * @param resource
     *            Resource to set an analysis error for.
     * @param exception
     *            Analysis error to set.
     */
    void error(FileObject resource, AnalysisException exception);

    /**
     * Sets an analysis error for sources in given parse result. Pushes analysis errors to subscribed requests.
     * 
     * @param parseResults
     *            Parse results with sources to set an analysis error for.
     * @param exception
     *            Analysis error to set.
     */
    void error(Iterable<ParseResult<P>> parseResults, AnalysisException exception);

    /**
     * Removes cached analysis results for given resource.
     * 
     * @param resource
     *            Resource to remove cached analysis results for.
     */
    void remove(FileObject resource);
}