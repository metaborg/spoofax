package org.metaborg.core.menu;

public interface IMenuItem {
    public abstract String name();

    /**
     * Accepts the specified visitor.
     *
     * @param visitor
     *            The visitor.
     */
    void accept(IMenuItemVisitor visitor);
}
