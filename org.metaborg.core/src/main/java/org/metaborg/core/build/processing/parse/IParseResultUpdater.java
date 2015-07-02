package org.metaborg.core.build.processing.parse;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;

/**
 * Interface for updating parse results which are requested by an {@link IParseResultRequester}.
 * 
 * @param <P>
 *            Type of parsed fragments.
 */
public interface IParseResultUpdater<P> {
    /**
     * Invalidates the parse result for given resource. Must be followed by a call to {@link #update} or {@link #error}
     * for that resource eventually. Failing to do so will block any request made while resource was in an invalid
     * state.
     * 
     * @param resource
     *            Resource to invalidate.
     */
    public abstract void invalidate(FileObject resource);

    /**
     * Updates the parse result for a single resource. Pushes the parse result to subscribed requests.
     * 
     * @param result
     *            Result to update.
     * @param parentResult
     *            Parent of the result to update.
     */
    public abstract void update(FileObject resource, ParseResult<P> result);

    /**
     * Sets a parse error for given resource. Pushes the parse error to subscribed requests.
     * 
     * @param resource
     *            Resource to set a parse error for.
     * @param exception
     *            Parse error to set.
     */
    public abstract void error(FileObject resource, ParseException exception);

    /**
     * Removes cached parse results for given resource.
     * 
     * @param resource
     *            Resource to remove cached parse results for.
     */
    public abstract void remove(FileObject resource);
}