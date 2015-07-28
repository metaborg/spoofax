package org.metaborg.core.resource;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for changes on resource trees between two discrete points in time. A tree represents a change in a
 * resource, and changes in child resources.
 */
public interface IResourceTreeChange {
    /**
     * Returns the resource that has changed.
     */
    public abstract FileObject resource();

    /**
     * Returns the kind of change.
     */
    public abstract ResourceChangeKind kind();

    /**
     * Returns changes in children of the changed resource. Only child resources with changes will be returned. The
     * returned changes are also trees, which can be traversed further.
     */
    public abstract Iterable<IResourceTreeChange> children();
}
