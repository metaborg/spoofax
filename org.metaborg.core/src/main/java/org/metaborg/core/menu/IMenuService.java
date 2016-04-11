package org.metaborg.core.menu;

import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for service that combines menus for language implementations.
 */
public interface IMenuService {
    /**
     * Gets the top-level menu items for given language implementation.
     * 
     * @param language
     *            Language implementation to get menu items for.
     * @return Top-level menu items.
     */
    Iterable<IMenuItem> menuItems(ILanguageImpl language);
}
