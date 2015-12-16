package org.metaborg.core.menu;

/**
 * Visitor for menu items.
 */
public interface IMenuItemVisitor {

    /**
     * Visits the menu.
     *
     * This method is called when no more specific visitor method applies.
     *
     * @param menu The menu to visit.
     */
    void visitMenu(IMenu menu);

    /**
     * Visits the action.
     *
     * This method is called when no more specific visitor method applies.
     *
     * @param action The action to visit.
     */
    void visitAction(IAction action);

    /**
     * Visits the separator.
     *
     * @param separator The separator to visit.
     */
    void visitSeparator(Separator separator);

    /**
     * Visits the menu item.
     *
     * This method is called when no more specific visitor method applies.
     *
     * @param item The menu item to visit.
     */
    void visitMenuItem(IMenuItem item);

}
