package org.metaborg.core.outline;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

/**
 * Interface representing a node in an outline tree view.
 */
public interface IOutlineNode {
    /**
     * @return Label
     */
    String label();

    /**
     * @return File object containing an icon, or null if there is no icon.
     */
    @Nullable FileObject icon();

    /**
     * @return Region in the source text this node originates from, or null if no region is available.
     */
    @Nullable ISourceRegion origin();

    /**
     * @return Parent node, or null if this is a root.
     */
    @Nullable IOutlineNode parent();

    /**
     * @return Sub nodes
     */
    Iterable<IOutlineNode> nodes();
}
