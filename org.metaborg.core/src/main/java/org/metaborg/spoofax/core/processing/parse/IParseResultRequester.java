package org.metaborg.spoofax.core.processing.parse;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ParseResult;

import rx.Observable;

/**
 * Interface for requesting single parse results or updates for parse results.
 * 
 * @param <P>
 *            Type of parsed fragments.
 */
public interface IParseResultRequester<P> {
    /**
     * Requests the parse result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest parse result, or pushes an error if an error occurred while getting it. If the parse
     * result is cached, the observable will immediately push it. If the parse result has been invalidated (when it is
     * in the process of being updated), it will be pushed when it has been updated. If there is no parse result yet, it
     * will parse with given text and push the parse result.
     * 
     * The simplest way to get the parse result is to wait for it:
     * {@code
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
    public abstract Observable<ParseResult<P>> request(FileObject resource, ILanguage language, String text);

    /**
     * Returns an observable that pushes parse result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    public abstract Observable<ParseChange<P>> updates(FileObject resource);

    /**
     * @return Latest parse result for given resource, or null if there is none.
     */
    public abstract @Nullable ParseResult<P> get(FileObject resource);
}