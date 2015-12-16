package org.metaborg.core.outline;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

/**
 * Interface representing a node in an outline tree view.
 */
public interface IOutlineNode {
    /**
     * @return Label
     */
    public abstract String label();

    /**
     * @return File object containing an icon, or null if there is no icon.
     */
    public abstract @Nullable FileObject icon();

    /**
     * @return Region in the source text this node originates from, or null if no region is available.
     */
    public abstract @Nullable ISourceRegion origin();

    /**
     * @return Parent node, or null if this is a root.
     */
    public abstract @Nullable IOutlineNode parent();

    /**
     * @return Sub nodes
     */
    public abstract Iterable<IOutlineNode> nodes();
}
