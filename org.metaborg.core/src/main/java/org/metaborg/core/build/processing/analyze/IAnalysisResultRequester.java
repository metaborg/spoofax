package org.metaborg.core.build.processing.analyze;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;

import rx.Observable;

/**
 * Interface for requesting single analysis results or updates for analysis results.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 */
public interface IAnalysisResultRequester<P, A> {
    /**
     * Requests the analysis result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest analysis result, or pushes an error if an error occurred while getting it. If the
     * analysis result is cached, the observable will immediately push it. If the analysis result has been invalidated
     * (when it is in the process of being updated), it will be pushed when it has been updated. If there is no analysis
     * result yet, it will request a parse result, analyze the resource in given context, and push the analysis result.
     * 
     * The simplest way to get the analysis result is to wait for it:
     * {@code
     *   result = requester.request(resource, context, text).toBlocking().single();
     * }
     * 
     * @param resource
     *            Resource to get the analysis result for.
     * @param context
     *            Context in which the analysis should be performed, in case there is no analysis result for given
     *            resource.
     * @param text
     *            Text which should be parsed, in case there is no parse result for given resource.
     * @return Cold observable which pushes a single element when subscribed; the latest analysis result, or pushes an
     *         error if an error occurred while getting it.
     */
    public abstract Observable<AnalysisFileResult<P, A>> request(FileObject resource, IContext context, String text);

    /**
     * Returns an observable that pushes analysis result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    public abstract Observable<AnalysisChange<P, A>> updates(FileObject resource);

    /**
     * @return Latest analysis result for given resource, or null if there is none.
     */
    public abstract @Nullable AnalysisFileResult<P, A> get(FileObject resource);
}