package org.metaborg.core.outline;

/**
 * Interface representing an outline.
 */
public interface IOutline {
    /**
     * @return Root node of the outline.
     */
    public abstract IOutlineNode root();

    /**
     * @return Automatically expand the outline returned levels deep.
     */
    public abstract int expandTo();
}
