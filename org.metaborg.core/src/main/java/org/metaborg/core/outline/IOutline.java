package org.metaborg.core.outline;

/**
 * Interface representing an outline.
 */
public interface IOutline {
    /**
     * @return Root nodes of the outline.
     */
    Iterable<IOutlineNode> roots();

    /**
     * @return Automatically expand the outline returned levels deep.
     */
    int expandTo();
}
