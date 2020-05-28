package org.metaborg.core.processing.analyze;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IInputUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * Interface for requesting single analysis results or updates for analysis results.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 */
public interface IAnalysisResultRequester<I extends IInputUnit, A extends IAnalyzeUnit> {
    /**
     * Requests the analysis result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest analysis result, or pushes an error if an error occurred while getting it. If the
     * analysis result is cached, the observable will immediately push it. If the analysis result has been invalidated
     * (when it is in the process of being updated), it will be pushed when it has been updated. If there is no analysis
     * result yet, it will request a parse result, analyze the resource in given context, and push the analysis result.
     * 
     * The simplest way to get the analysis result is to wait for it: {@code
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
    Observable<A> request(I input, IContext context);

    /**
     * Returns an observable that pushes analysis result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    Observable<AnalysisChange<A>> updates(FileObject resource);

    /**
     * @return Latest analysis result for given resource, or null if there is none.
     */
    @Nullable A get(FileObject resource);
}
