package org.metaborg.core.menu;

public interface IMenuItem {
    /**
     * @return Name of the menu item.
     */
    public abstract String name();

    /**
     * Accepts the specified visitor.
     *
     * @param visitor
     *            The visitor.
     */
    void accept(IMenuItemVisitor visitor);
}
