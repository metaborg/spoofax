package org.metaborg.core.source;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Represents a region in a resource.
 */
public interface ISourceLocation {
    /**
     * @return Region in the source file.
     */
    public abstract ISourceRegion region();

    /**
     * @return Resource of the source file, or null if the source file could not be determined.
     */
    public abstract @Nullable FileObject resource();
}
