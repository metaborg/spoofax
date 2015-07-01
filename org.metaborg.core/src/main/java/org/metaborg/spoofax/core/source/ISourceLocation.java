package org.metaborg.spoofax.core.source;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Represents a region in a resource.
 */
public interface ISourceLocation {
    /**
     * @return Source region
     */
    public abstract ISourceRegion region();

    /**
     * @return Resource, or null if the resource could not be determined.
     */
    public abstract @Nullable FileObject resource();
}
