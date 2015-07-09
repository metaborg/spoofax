package org.metaborg.core.resource;

import rx.Observable;

/**
 * Interface for subscribing to resource changes.
 */
public interface IResourceChangeService {
    /**
     * Returns an observable over resource changes. This returns a 'hot' observable, meaning that any changes that
     * occurred before subscribing are NOT observed.
     */
    public abstract Observable<ResourceChange> changes();
}
