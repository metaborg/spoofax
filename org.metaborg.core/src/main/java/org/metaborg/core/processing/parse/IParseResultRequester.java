package org.metaborg.core.processing.parse;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;

import rx.Observable;

/**
 * Interface for requesting single parse results or updates for parse results.
 */
public interface IParseResultRequester<I extends IInputUnit, P extends IParseUnit> {
    /**
     * Requests the parse result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest parse result, or pushes an error if an error occurred while getting it. If the parse
     * result is cached, the observable will immediately push it. If the parse result has been invalidated (when it is
     * in the process of being updated), it will be pushed when it has been updated. If there is no parse result yet, it
     * will parse with given text and push the parse result.
     * 
     * The simplest way to get the parse result is to wait for it: {@code
     *   result = requester.request(resource, language, text).toBlocking().single();
     * }
     * 
     * @param resource
     *            Resource to get the parse result for.
     * @param language
     *            Language given text should be parsed with, in case there is no parse result for given resource.
     * @param text
     *            Text which should be parsed, in case there is no parse result for given resource.
     * @return Cold observable which pushes a single element when subscribed; the latest parse result, or pushes an
     *         error if an error occurred while getting it.
     */
    Observable<P> request(I unit);

    /**
     * Returns an observable that pushes parse result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    Observable<ParseChange<P>> updates(FileObject resource);

    /**
     * @return Latest parse result for given resource, or null if there is none.
     */
    @Nullable P get(FileObject resource);
}